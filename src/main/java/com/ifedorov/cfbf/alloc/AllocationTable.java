package com.ifedorov.cfbf.alloc;

import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.ifedorov.cfbf.Sector;
import com.ifedorov.cfbf.Sectors;
import com.ifedorov.cfbf.Utils;

import java.util.List;

public class AllocationTable {

    public static final Integer ENTRIES_IN_ONE_FAT_SECTOR = 128;
    protected final Sectors sectors;
    protected final List<Integer> sectorChain;
    private final int sectorSize;

    public AllocationTable(Sectors sectors, List<Integer> sectorChain, int sectorSize) {
        this.sectors = sectors;
        this.sectorChain = sectorChain;
        this.sectorSize = sectorSize;
    }

    public List<Integer> buildChain(int currentSector) {
        if(Utils.isEndOfChain(currentSector)) {
            return Lists.newArrayList();
        }
        List<Integer> chain = Lists.newArrayList();
        chain.add(currentSector);
        try {
            while (!Utils.isEndOfChain(currentSector = getValueAt(currentSector))) {
                chain.add(currentSector);
            }
        } catch (IndexOutOfBoundsException e) {
            throw e;
        }
        return chain;
    }

    private int getValueAt(int position) {
        int sectorNumber = position * 4 / sectorSize;
        int shiftInsideSector = position * 4 % sectorSize;
        Verify.verify(sectorNumber <= sectorChain.size());
        return Utils.toInt(sectors.sector(sectorChain.get(sectorNumber)).subView(shiftInsideSector, shiftInsideSector + 4).getData());
    }

    public void registerSector(Integer sectorPosition, Integer previousSectorPosition) {
        getFatSectorPointingToAllocatedSector(sectorPosition).writeAt(calculatePositionInsideFatSector(sectorPosition), Utils.ENDOFCHAIN_MARK);
        if(previousSectorPosition != null) {
            getFatSectorPointingToAllocatedSector(previousSectorPosition).writeAt(calculatePositionInsideFatSector(previousSectorPosition), Utils.toBytes(sectorPosition, 4));
        }
    }

    protected Sector getFatSectorPointingToAllocatedSector(Integer sectorPosition) {
        Integer fatSectorInChain = sectorPosition / ENTRIES_IN_ONE_FAT_SECTOR;
        if(sectorChain.size() <= fatSectorInChain) {
            Sector targetSector = null;
            while(sectorChain.size() <= fatSectorInChain) {
                targetSector = allocateNewSector();
            }
            return targetSector;
        } else {
            return sectors.sector(sectorChain.get(fatSectorInChain));
        }
    }

    protected Sector allocateNewSector() {
        Sector fatSector = sectors.allocate();
        int sectorPosition = fatSector.getPosition();
        sectorChain.add(sectorPosition);
        return fatSector;
    }

    protected Integer calculatePositionInsideFatSector(Integer sectorPosition) {
        return sectorPosition % ENTRIES_IN_ONE_FAT_SECTOR * 4;
    }

}