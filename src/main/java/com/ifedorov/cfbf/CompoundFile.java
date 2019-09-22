package com.ifedorov.cfbf;

import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.ifedorov.cfbf.stream.ConditionalStreamReader;
import com.ifedorov.cfbf.stream.MiniStreamReader;
import com.ifedorov.cfbf.stream.RegularStreamReader;

import java.util.Arrays;
import java.util.List;
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
        difat = new DIFAT(sectors, header);
        fat = new FAT(difat.getFatSectorChain().stream().map((num) -> sectors.sector(num)).collect(Collectors.toList()), header.getSectorShift());
        miniFat = new MiniFAT(fat.buildChain(header.getFirstMinifatSectorLocation()).stream().map((num) -> sectors.sector(num)).collect(Collectors.toList()), header.getSectorShift());
        ConditionalStreamReader streamReader = new ConditionalStreamReader(
                new RegularStreamReader(fat, sectors),
                new MiniStreamReader(miniFat, fat.buildChain(getMiniStreamFirstSectorLocation()).stream().map((num) -> sectors.sector(num)).collect(Collectors.toList()),sectors, header.getMiniSectorShift(), header.getSectorShift()),
                header.getMiniStreamCutoffSize()
        );
        directoryEntryChain = new DirectoryEntryChain(fat.buildChain(header.getFirstDirectorySectorLocation()).stream().map((num) -> sectors.sector(num)).collect(Collectors.toList()), streamReader);
    }

    public Header header() {
        return header;
    }

    private int getMiniStreamFirstSectorLocation() {
        return Utils.toInt(sectors.sector(header.getFirstDirectorySectorLocation()).subView(DirectoryEntry.FLAG_POSITION.STARTING_SECTOR_LOCATION, DirectoryEntry.FLAG_POSITION.STARTING_SECTOR_LOCATION + 4).getData());
    }

    public DirectoryEntry getRootStorage() {
        return directoryEntryChain.getEntryById(0);
    }

}
