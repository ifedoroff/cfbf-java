package com.ifedorov.cfbf.alloc;

import com.ifedorov.cfbf.Header;
import com.ifedorov.cfbf.Sectors;

public class FAT extends AllocationTable {

    private final Header header;
    private final FATtoDIFATFacade difat;

    public FAT(Sectors sectors, Header header, FATtoDIFATFacade difat) {
        super(sectors, difat.getFatSectorChain(), header.getSectorShift());
        this.header = header;
        this.difat = difat;
    }
}
