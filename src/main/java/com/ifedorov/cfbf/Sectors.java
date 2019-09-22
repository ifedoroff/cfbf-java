package com.ifedorov.cfbf;

import com.google.common.base.Verify;
import com.google.common.collect.Lists;

import java.util.List;

public class Sectors {

    private DataView dataView;
    private int sectorShift;
    private final List<Sector> sectors = Lists.newArrayList();

    public Sectors(DataView dataView, int sectorShift) {
        this.dataView = dataView;
        this.sectorShift = sectorShift;
        readSectors();
    }

    public Sector sector(int position) {
        return sectors.get(position);
    }

    private void readSectors() {
        //Skip first 512 bytes designated for Header
        DataView sectorsDataView = this.dataView.subView(512);
        Verify.verify(sectorsDataView.getSize() % sectorShift == 0);
        for (int i = 0; i < sectorsDataView.getSize() / sectorShift; i++) {
            sectors.add(Sector.from(sectorsDataView.subView(i * sectorShift, (i+1)* sectorShift), sectors.size()));
        }
    }
}
