package com.ifedorov.cfbf;

import com.ifedorov.cfbf.stream.StreamReader;

import java.util.List;

public class DirectoryEntryChain {

    private List<Sector> sectors;
    private StreamReader streamReader;

    public DirectoryEntryChain(List<Sector> sectors, StreamReader streamReader) {
        this.sectors = sectors;
        this.streamReader = streamReader;
    }

    public DirectoryEntry getEntryById(int i) {
        int sectorNumber = i / 4;
        int shiftInsideSector = i % 4 * 128;
        return new DirectoryEntry(this, sectors.get(sectorNumber).subView(shiftInsideSector, shiftInsideSector + 128), streamReader);
    }
}
