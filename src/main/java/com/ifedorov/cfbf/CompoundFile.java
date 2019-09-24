package com.ifedorov.cfbf;

import com.ifedorov.cfbf.stream.ConditionalStreamRW;
import com.ifedorov.cfbf.stream.MiniStreamRW;
import com.ifedorov.cfbf.stream.RegularStreamRW;

import java.util.stream.Collectors;

public class CompoundFile {

    private final Header header;
    private final DIFAT difat;
    private final Sectors sectors;
    private final FAT fat;
    private final MiniFAT miniFat;
    private final DirectoryEntryChain directoryEntryChain;
    private DataView dataView;

    public CompoundFile(DataView dataView) {
        this.dataView = dataView;
        this.header = new Header(dataView.subView(0, Header.HEADER_LENGTH));
        this.sectors = new Sectors(dataView, header.getSectorShift());
        difat = new DIFAT(this, header);
        fat = new FAT(this, difat.getFatSectorChain(), header.getSectorShift());
        miniFat = new MiniFAT(this, fat.buildChain(header.getFirstMinifatSectorLocation()), header.getSectorShift());
        ConditionalStreamRW streamReader = new ConditionalStreamRW(
                new RegularStreamRW(fat, this),
                new MiniStreamRW(miniFat, fat.buildChain(getMiniStreamFirstSectorLocation()),this, header.getMiniSectorShift(), header.getSectorShift()),
                header.getMiniStreamCutoffSize()
        );
        directoryEntryChain = new DirectoryEntryChain(this, fat.buildChain(header.getFirstDirectorySectorLocation()).stream().collect(Collectors.toList()), streamReader);
    }

    private int getMiniStreamFirstSectorLocation() {
        return Utils.toInt(sectors.sector(header.getFirstDirectorySectorLocation()).subView(DirectoryEntry.FLAG_POSITION.STARTING_SECTOR_LOCATION, DirectoryEntry.FLAG_POSITION.STARTING_SECTOR_LOCATION + 4).getData());
    }

    public DirectoryEntry getRootStorage() {
        return directoryEntryChain.getEntryById(0);
    }

    public Sector sector(int position) {
        return this.sectors.sector(position);
    }

}
