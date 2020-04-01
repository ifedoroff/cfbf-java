package com.ifedorov.cfbf.stream;

import com.google.common.collect.Iterables;
import com.ifedorov.cfbf.DataView;
import com.ifedorov.cfbf.Header;
import com.ifedorov.cfbf.Sectors;
import com.ifedorov.cfbf.alloc.FAT;
import com.ifedorov.cfbf.Sector;
import org.apache.commons.lang3.ArrayUtils;

import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

public class RegularStreamRW implements StreamRW {

    private final FAT fat;
    private final Sectors sectors;
    private Header header;

    public RegularStreamRW(FAT fat, Sectors sectors, Header header) {
        this.fat = fat;
        this.sectors = sectors;
        this.header = header;
    }

    @Override
    public byte[] read(int startingSector, int length) {
        byte[] result = new byte[length];
        int positionInResult = 0;
        for (Integer sectorPosition : fat.buildChain(startingSector)) {
            if(length > 0) {
                Sector sector = sectors.sector(sectorPosition);
                int bytesToRead = Math.min(sector.getSize(), length);
                System.arraycopy(sector.subView(0, bytesToRead).getData(), 0, result, positionInResult, bytesToRead);
                positionInResult += bytesToRead;
                length -= bytesToRead;
            } else {
                break;
            }
        }
        return result;
    }

    @Override
    public byte[] read(int startingSector, int fromIncl, int toExcl) {
        return new DataView.VariableSizeChunkedDataView(fat.buildChain(startingSector).stream().map(sectors::sector).collect(Collectors.toList()))
                .subView(fromIncl, toExcl).getData();
    }

    @Override
    public int write(byte[] data) {
        Integer firstSectorPosition = null;
        Integer previousSectorPosition = null;
        for (int i = 0; i < data.length; i+=header.getSectorShift()) {
            Sector sector = sectors.allocate();
            int writeBytes = Math.min(header.getSectorShift(), data.length - i);
            sector.writeAt(0, ArrayUtils.subarray(data, i, i + writeBytes));
            int sectorPosition = sector.getPosition();
            fat.registerSector(sectorPosition, previousSectorPosition);
            if(firstSectorPosition == null) {
                firstSectorPosition = sectorPosition;
            }
            previousSectorPosition = sectorPosition;
        }
        return firstSectorPosition;
    }

    @Override
    public void writeAt(int startingSector, int position, byte[] data) {
        new DataView.VariableSizeChunkedDataView(fat.buildChain(startingSector).stream().map(sectors::sector).collect(Collectors.toList()))
                .writeAt(position, data);
    }

    @Override
    public int append(int startingSector, int currentSize, byte[] data) {
        List<Integer> sectorChain = fat.buildChain(startingSector);
        if(sectorChain.isEmpty()) {
            return write(data);
        }
        Integer lastSectorPosition = Iterables.getLast(sectorChain);
        DataView lastSector = sectors.sector(lastSectorPosition);
        int freeBytesInLastSector = 0;
        int remainingBytes = data.length;
        if(currentSize % header.getSectorShift() != 0) {
            freeBytesInLastSector = lastSector.getSize() - currentSize % header.getSectorShift();
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
        Integer previousSectorPosition = lastSectorPosition;
        for (int i = 0; i < numberOfChunks; i+=header.getSectorShift()) {
            Sector sector = this.sectors.allocate();
            int writeBytes = Math.min(header.getSectorShift(), data.length - i);
            sector.writeAt(0, ArrayUtils.subarray(data, i, i + writeBytes));
            int sectorPosition = sector.getPosition();
            fat.registerSector(sectorPosition, previousSectorPosition);
            previousSectorPosition = sectorPosition;
        }
        return startingSector;
    }

    @Override
    public void copyTo(int startingSector, OutputStream os) {
        new DataView.VariableSizeChunkedDataView(fat.buildChain(startingSector).stream().map(sectors::sector).collect(Collectors.toList()))
                .copyTo(os);
    }

    private int howManyChunksNeeded(int dataLength) {
        int numberOfChunks = -1;
        if(dataLength % header.getSectorShift() == 0) {
            numberOfChunks = dataLength / header.getSectorShift();
        } else {
            numberOfChunks = dataLength / header.getSectorShift() + 1;
        }
        return numberOfChunks;
    }
}
