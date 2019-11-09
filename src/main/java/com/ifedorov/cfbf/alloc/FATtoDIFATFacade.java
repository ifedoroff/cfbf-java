package com.ifedorov.cfbf.alloc;

import java.util.List;

public class FATtoDIFATFacade {

    private DIFAT difat;
    private FAT fat;

    public void setDifat(DIFAT difat) {
        this.difat = difat;
    }

    public void setFat(FAT fat) {
        this.fat = fat;
    }

    public List<Integer> getFatSectorChain() {
        return difat.getFatSectorChain();
    }

    public void registerFatSectorInDIFAT(int sectorPosition) {
        difat.registerFATSector(sectorPosition);
    }

    public void registerDifatSectorInFAT(int sectorPosition) {
        fat.registerDifatSector(sectorPosition);
    }
}
