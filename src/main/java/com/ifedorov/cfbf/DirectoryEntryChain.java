package com.ifedorov.cfbf;

import com.ifedorov.cfbf.alloc.FAT;
import com.ifedorov.cfbf.stream.StreamRW;
import com.ifedorov.cfbf.stream.StreamReader;

import java.util.List;
import java.util.stream.Collectors;

public class DirectoryEntryChain {

    private Sectors sectors;
    private FAT fat;
    private Header header;
    private List<Integer> sectorChain;
    private StreamRW streamReader;

    public DirectoryEntryChain(Sectors sectors, FAT fat, Header header, StreamRW streamReader) {
        this.sectors = sectors;
        this.fat = fat;
        this.header = header;
        this.sectorChain = fat.buildChain(header.getFirstDirectorySectorLocation()).stream().collect(Collectors.toList());
        this.streamReader = streamReader;
//        readDirectorySectorsCount();
    }

//    private int readDirectorySectorsCount() {
//        Sector lastDirectoryEntrySector = sectors.sector(sectorChain.get(sectorChain.size() - 1));
//        for (int i = 0; i < header.getSectorShift(); i+=128) {
//
//        }
//    }

    public DirectoryEntry getEntryById(int i) {
        int sectorNumber = i / 4;
        int shiftInsideSector = i % 4 * 128;
        return new DirectoryEntry(i, this, sectors.sector(sectorChain.get(sectorNumber)).subView(shiftInsideSector, shiftInsideSector + 128), streamReader);
    }

//    public DirectoryEntry createDirectoryEntry() {
//
//    }
}
