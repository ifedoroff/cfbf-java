package com.ifedorov.cfbf.alloc;

import com.ifedorov.cfbf.Header;
import com.ifedorov.cfbf.Sector;
import com.ifedorov.cfbf.Sectors;
import com.ifedorov.cfbf.Utils;

public class FAT extends AllocationTable {

    private final Header header;
    private final FATtoDIFATFacade difat;

    public FAT(Sectors sectors, Header header, FATtoDIFATFacade difat) {
        super(sectors, difat.getFatSectorChain(), header.getSectorShift());
        this.header = header;
        this.difat = difat;
    }

    public void registerDifatSector(Integer position) {
        getFatSectorPointingToAllocatedSector(position).writeAt(calculatePositionInsideFatSector(position), Utils.DISECT_MARK);
    }

    @Override
    protected Sector allocateNewFatSector() {
        Sector newSector = super.allocateNewFatSector();
        difat.registerFatSectorInDIFAT(newSector.getPosition());
        return newSector;
    }
}
