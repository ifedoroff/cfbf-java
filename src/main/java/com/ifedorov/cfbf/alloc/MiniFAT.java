package com.ifedorov.cfbf.alloc;

import com.ifedorov.cfbf.Header;
import com.ifedorov.cfbf.Sectors;

public class MiniFAT extends AllocationTable {

    private final Header header;
    private final FAT fat;

    public MiniFAT(Sectors sectors, Header header, FAT fat) {
        super(sectors, fat.buildChain(header.getFirstMinifatSectorLocation()), header.getSectorShift());
        this.header = header;
        this.fat = fat;
    }
}
