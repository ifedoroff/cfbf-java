package com.ifedorov.cfbf.stream;

import com.google.common.base.Verify;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.ifedorov.cfbf.*;
import com.ifedorov.cfbf.alloc.FAT;
import com.ifedorov.cfbf.alloc.MiniFAT;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.stream.Collectors;

public class MiniStreamRW implements StreamRW {

    public static final int MINI_STREAM_CHUNK_SIZE = 64;
    private final MiniFAT miniFAT;
    private final Header header;
    private int miniStreamLength;
    private FAT fat;
    private List<Integer> miniStreamSectorChain;
    private final Sectors sectors;

    public MiniStreamRW(MiniFAT miniFAT, FAT fat, int firstMiniStreamSector, int miniStreamLength, Sectors sectors, Header header) {
        this.miniFAT = miniFAT;
        this.fat = fat;
        this.miniStreamLength = miniStreamLength;
        if(firstMiniStreamSector >= 0) {
            this.miniStreamSectorChain = fat.buildChain(firstMiniStreamSector);
        } else {
            this.miniStreamSectorChain = Lists.newArrayList();
        }
        this.sectors = sectors;
        this.header = header;
    }

    @Override
    public byte[] read(int startingSector, int length) {
        byte[] result = new byte[length];
        int position = 0;
        for (Integer sectorNumber : miniFAT.buildChain(startingSector)) {
            if(length > 0) {
                DataView data = getMiniSectorData(sectorNumber);
                int bytesToRead = Math.min(data.getSize(), length);
                System.arraycopy(data.subView(0, bytesToRead).getData(), 0, result, position, bytesToRead);
                position += bytesToRead;
                length -= bytesToRead;
            } else {
                break;
            }
        }
        return result;
    }

    @Override
    public byte[] read(int startingSector, int fromIncl, int toExcl) {
        return new DataView.VariableSizeChunkedDataView(miniFAT.buildChain(startingSector).stream().map(this::getMiniSectorData).collect(Collectors.toList()))
                .subView(fromIncl, toExcl).getData();
    }

    private DataView getMiniSectorData(int position) {
        int sectorPosition = position * header.getMiniSectorShift() / header.getSectorShift();
        int shiftInsideSector = position * header.getMiniSectorShift() % header.getSectorShift();
        return sectors.sector(miniStreamSectorChain.get(sectorPosition)).subView(shiftInsideSector, shiftInsideSector + header.getMiniSectorShift());
    }

    @Override
    public int write(byte[] data) {
        Verify.verify(data.length > 0);
        int numberOfChunks = howManyChunksNeeded(data.length);
        int firstMiniSectorPosition = Utils.ENDOFCHAIN_MARK_INT;
        for (int i = 0; i < numberOfChunks; i++) {
            int bytesFromPosition = i * header.getMiniSectorShift();
            int bytesUpToPosition = Math.min((i + 1) * header.getMiniSectorShift(), data.length);
            byte[] bytesToWrite = ArrayUtils.subarray(data, bytesFromPosition, bytesUpToPosition);
            getDataHolderForNextChunk().writeAt(0, bytesToWrite);
            int miniSectorPosition = miniStreamLength / header.getMiniSectorShift();
            if(firstMiniSectorPosition == Utils.ENDOFCHAIN_MARK_INT) {
                firstMiniSectorPosition = miniSectorPosition;
            }
            if(i == 0) {
                miniFAT.registerSector(miniSectorPosition, null);
            } else {
                miniFAT.registerSector(miniSectorPosition, miniSectorPosition - 1);
            }
            miniStreamLength += header.getMiniSectorShift();
        }
        return firstMiniSectorPosition;
    }

    private int howManyChunksNeeded(int dataLength) {
        int numberOfChunks = -1;
        if(dataLength % header.getMiniSectorShift() == 0) {
            numberOfChunks = dataLength / header.getMiniSectorShift();
        } else {
            numberOfChunks = dataLength / header.getMiniSectorShift() + 1;
        }
        return numberOfChunks;
    }

    @Override
    public void writeAt(int startingSector, int position, byte[] data) {
        new DataView.VariableSizeChunkedDataView(miniFAT.buildChain(startingSector).stream().map(this::getMiniSectorData).collect(Collectors.toList()))
                .writeAt(position, data);
    }

    @Override
    public int append(int startingSector, int currentSize, byte[] data) {
        List<Integer> sectorChain = miniFAT.buildChain(startingSector);
        if(sectorChain.isEmpty()) {
            return write(data);
        }
        Integer lastSectorPosition = Iterables.getLast(sectorChain);
        DataView lastSector = getMiniSectorData(lastSectorPosition);
        int freeBytesInLastSector = 0;
        int remainingBytes = data.length;
        if(currentSize % header.getMiniSectorShift() != 0) {
            freeBytesInLastSector = lastSector.getSize() - currentSize % header.getMiniSectorShift();
            if(freeBytesInLastSector > 0) {
                int byteToWrite = Math.min(freeBytesInLastSector, data.length);
                lastSector.writeAt(lastSector.getSize() - freeBytesInLastSector, ArrayUtils.subarray(data, 0, byteToWrite));
                freeBytesInLastSector -= byteToWrite;
                remainingBytes -= byteToWrite;
            }
        }
        if(freeBytesInLastSector > 0 || remainingBytes == 0) {
            return startingSector;
        }
        int numberOfChunks = howManyChunksNeeded(remainingBytes);
        for (int i = 0; i < numberOfChunks; i++) {
            int bytesFromPosition = i * header.getMiniSectorShift();
            int bytesUpToPosition = Math.min((i + 1) * header.getMiniSectorShift(), data.length);
            byte[] bytesToWrite = ArrayUtils.subarray(data, bytesFromPosition, bytesUpToPosition);
            getDataHolderForNextChunk().writeAt(0, bytesToWrite);
            int miniSectorPosition = miniStreamLength / header.getMiniSectorShift();
            if(i == 0) {
                miniFAT.registerSector(miniSectorPosition, lastSectorPosition);
            } else {
                miniFAT.registerSector(miniSectorPosition, miniSectorPosition - 1);
            }
            miniStreamLength += header.getMiniSectorShift();
        }
        return startingSector;
    }

    private DataView getDataHolderForNextChunk() {
        Sector currentSector = getSectorForNextChunk();
        int positionInCurrentSector = miniStreamLength % header.getSectorShift();
        return currentSector.subView(positionInCurrentSector, positionInCurrentSector + header.getMiniSectorShift());
    }

    private Sector getSectorForNextChunk() {
        if(miniStreamSectorChain.isEmpty()) {
            Sector sector = sectors.allocate();
            fat.registerSector(sector.getPosition(), null);
            miniStreamSectorChain.add(sector.getPosition());
            return sector;
        } else if(miniStreamLength % header.getSectorShift() == 0) {
            Sector sector = sectors.allocate();
            fat.registerSector(sector.getPosition(), sectors.sector(miniStreamSectorChain.get(miniStreamSectorChain.size() - 1)).getPosition());
            miniStreamSectorChain.add(sector.getPosition());
            return sector;
        } else {
            return sectors.sector(miniStreamSectorChain.get(miniStreamSectorChain.size() - 1));
        }
    }

    public int getMiniStreamLength() {
        return miniStreamLength;
    }

    public int getMiniStreamFirstSectorPosition() {
        return miniStreamLength <= 0 ? Utils.toInt(Utils.FREESECT_MARK_OR_NOSTREAM) : miniStreamSectorChain.get(0);
    }
}
