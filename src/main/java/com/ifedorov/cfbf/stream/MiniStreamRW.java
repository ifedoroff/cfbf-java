package com.ifedorov.cfbf.stream;

import com.ifedorov.cfbf.*;

import java.util.List;

public class MiniStreamRW implements StreamRW {

    private final MiniFAT miniFAT;
    private List<Integer> miniStreamSectorChain;
    private final CompoundFile compoundFile;
    private int miniSectorSize;
    private int sectorSize;

    public MiniStreamRW(MiniFAT miniFAT, List<Integer> miniStreamSectorChain, CompoundFile compoundFile, int miniSectorSize, int sectorSize) {
        this.miniFAT = miniFAT;
        this.miniStreamSectorChain = miniStreamSectorChain;
        this.compoundFile = compoundFile;
        this.miniSectorSize = miniSectorSize;
        this.sectorSize = sectorSize;
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
        int sectorPosition = position * miniSectorSize / sectorSize;
        int shiftInsideSector = position * miniSectorSize % sectorSize;
        return compoundFile.sector(miniStreamSectorChain.get(sectorPosition)).subView(shiftInsideSector, shiftInsideSector + miniSectorSize);
    }

    @Override
    public int write(byte[] data) {
        throw new UnsupportedOperationException("Not supported yet");
    }
}
