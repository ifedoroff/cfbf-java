package com.ifedorov.cfbf.stream;

import com.ifedorov.cfbf.FAT;
import com.ifedorov.cfbf.Sector;
import com.ifedorov.cfbf.Sectors;

public class RegularStreamReader implements StreamReader{

    private final FAT fat;
    private final Sectors sectors;

    public RegularStreamReader(FAT fat, Sectors sectors) {
        this.fat = fat;
        this.sectors = sectors;
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
}
