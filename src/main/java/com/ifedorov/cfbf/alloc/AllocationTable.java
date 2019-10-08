package com.ifedorov.cfbf.alloc;

import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.ifedorov.cfbf.Sectors;
import com.ifedorov.cfbf.Utils;

import java.util.List;

public class AllocationTable {

    private final Sectors sectors;
    private final List<Integer> sectorChain;
    private final int sectorSize;

    public AllocationTable(Sectors sectors, List<Integer> sectorChain, int sectorSize) {
        this.sectors = sectors;
        this.sectorChain = sectorChain;
        this.sectorSize = sectorSize;
    }

    public List<Integer> buildChain(int currentSector) {
        List<Integer> chain = Lists.newArrayList();
        chain.add(currentSector);
        while(!Utils.isEndOfChain(currentSector = getValueAt(currentSector))) {
            chain.add(currentSector);
        }
        return chain;
    }

    private int getValueAt(int position) {
        int sectorNumber = position * 4 / sectorSize;
        int shiftInsideSector = position * 4 % sectorSize;
        Verify.verify(sectorNumber <= sectorChain.size());
        return Utils.toInt(sectors.sector(sectorChain.get(sectorNumber)).subView(shiftInsideSector, shiftInsideSector + 4).getData());
    }
}
