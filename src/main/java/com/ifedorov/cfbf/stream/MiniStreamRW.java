package com.ifedorov.cfbf.stream;

import com.ifedorov.cfbf.*;
import com.ifedorov.cfbf.alloc.MiniFAT;

import java.util.List;

public class MiniStreamRW implements StreamRW {

    private final MiniFAT miniFAT;
    private final Header header;
    private List<Integer> miniStreamSectorChain;
    private final Sectors sectors;

    public MiniStreamRW(MiniFAT miniFAT, List<Integer> miniStreamSectorChain, Sectors sectors, Header header) {
        this.miniFAT = miniFAT;
        this.miniStreamSectorChain = miniStreamSectorChain;
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
        throw new UnsupportedOperationException("Not supported yet");
    }
}
