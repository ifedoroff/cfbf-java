package com.ifedorov.cfbf;

import com.ifedorov.cfbf.stream.StreamReader;

import java.util.List;

public class DirectoryEntryChain {

    private CompoundFile compoundFile;
    private List<Integer> sectorChain;
    private StreamReader streamReader;

    public DirectoryEntryChain(CompoundFile compoundFile, List<Integer> sectorChain, StreamReader streamReader) {
        this.compoundFile = compoundFile;
        this.sectorChain = sectorChain;
        this.streamReader = streamReader;
    }

    public DirectoryEntry getEntryById(int i) {
        int sectorNumber = i / 4;
        int shiftInsideSector = i % 4 * 128;
        return new DirectoryEntry(this, compoundFile.sector(sectorChain.get(sectorNumber)).subView(shiftInsideSector, shiftInsideSector + 128), streamReader);
    }
}
