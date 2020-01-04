package com.ifedorov.cfbf;

import com.google.common.base.Verify;
import com.google.common.collect.Lists;

import java.util.List;

import static com.ifedorov.cfbf.Header.HEADER_LENGTH;

public class Sectors {

    private DataView dataView;
    private int sectorShift;
    private Header header;
    private final List<Sector> sectors = Lists.newArrayList();

    public Sectors(DataView dataView, Header header) {
        this.dataView = dataView;
        this.sectorShift = header.getSectorShift();
        this.header = header;
        readSectors();
    }

    public Sector sector(int position) {
        return sectors.get(position);
    }

    private void readSectors() {
        //Skip first 512 bytes designated for Header
        if(!this.dataView.isEmpty()) {
            DataView sectorsDataView = this.dataView.subView(header.getSectorShift());
            Verify.verify(sectorsDataView.getSize() % sectorShift == 0);
            for (int i = 0; i < sectorsDataView.getSize() / sectorShift; i++) {
                sectors.add(Sector.from(sectorsDataView.subView(i * sectorShift, (i + 1) * sectorShift), sectors.size()));
            }
        }
    }

    public Sector allocate() {
        Sector allocated = Sector.from(dataView.allocate(header.getSectorShift()), sectors.size());
        allocated.fill(Utils.FREESECT_MARK_OR_NOSTREAM);
        sectors.add(allocated);
        return allocated;
    }

    public DIFATSector allocateDIFAT() {
        DIFATSector sector = new DIFATSector(allocate());
        sector.fill(Utils.FREESECT_MARK_OR_NOSTREAM);
        sector.subView(508).writeAt(0, Utils.ENDOFCHAIN_MARK);
        return sector;
    }
}
