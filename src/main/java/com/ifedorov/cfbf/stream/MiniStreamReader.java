package com.ifedorov.cfbf.stream;

import com.ifedorov.cfbf.*;

import java.util.List;

public class MiniStreamReader implements StreamReader {

    private final MiniFAT miniFAT;
    private List<Sector> miniStreamSectors;
    private final Sectors sectors;
    private int miniSectorSize;
    private int sectorSize;

    public MiniStreamReader(MiniFAT miniFAT, List<Sector> miniStreamSectors, Sectors sectors, int miniSectorSize, int sectorSize) {
        this.miniFAT = miniFAT;
        this.miniStreamSectors = miniStreamSectors;
        this.sectors = sectors;
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
        return miniStreamSectors.get(sectorPosition).subView(shiftInsideSector, shiftInsideSector + miniSectorSize);
    }
}
