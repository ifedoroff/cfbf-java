package com.ifedorov.cfbf.stream;

import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.ifedorov.cfbf.*;
import com.ifedorov.cfbf.alloc.FAT;
import com.ifedorov.cfbf.alloc.MiniFAT;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

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

    private DataView getMiniSectorData(int position) {
        int sectorPosition = position * header.getMiniSectorShift() / header.getSectorShift();
        int shiftInsideSector = position * header.getMiniSectorShift() % header.getSectorShift();
        return sectors.sector(miniStreamSectorChain.get(sectorPosition)).subView(shiftInsideSector, shiftInsideSector + header.getMiniSectorShift());
    }

    @Override
    public int write(byte[] data) {
        Verify.verify(data.length > 0);
        int numberOfChunks = -1;
        if(data.length % header.getMiniSectorShift() == 0) {
            numberOfChunks = data.length / header.getMiniSectorShift();
        } else {
            numberOfChunks = data.length / header.getMiniSectorShift() + 1;
        }
        int firstMiniSectorPosition = -1;
        for (int i = 0; i < numberOfChunks; i++) {
            int bytesFromPosition = i * header.getMiniSectorShift();
            int bytesUpToPosition = Math.min((i + 1) * header.getMiniSectorShift(), data.length);
            byte[] bytesToWrite = ArrayUtils.subarray(data, bytesFromPosition, bytesUpToPosition);
            getDataHolderForNextChunk().writeAt(0, bytesToWrite);
            int miniSectorPosition = miniStreamLength / header.getMiniSectorShift();
            if(firstMiniSectorPosition == -1) {
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
