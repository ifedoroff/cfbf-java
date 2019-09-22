package com.ifedorov.cfbf;

import com.google.common.base.Verify;
import com.google.common.collect.Lists;

import java.util.List;

public class FAT extends AllocationTable{

    public FAT(List<Sector> sectors, int sectorSize) {
        super(sectors, sectorSize);
    }
}
