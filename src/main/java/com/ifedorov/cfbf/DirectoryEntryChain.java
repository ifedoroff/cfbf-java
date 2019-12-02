package com.ifedorov.cfbf;

import com.google.common.collect.Lists;
import com.ifedorov.cfbf.alloc.FAT;
import com.ifedorov.cfbf.stream.StreamRW;

import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ifedorov.cfbf.DirectoryEntry.UTF16_TERMINATING_BYTES;

public class DirectoryEntryChain {

    private Sectors sectors;
    private FAT fat;
    private Header header;
    private LinkedList<Integer> sectorChain;
    private StreamRW streamReader;
    private int directoryEntryCount;

    public DirectoryEntryChain(Sectors sectors, FAT fat, Header header, StreamRW streamReader) {
        this.sectors = sectors;
        this.fat = fat;
        this.header = header;
        this.sectorChain = Lists.newLinkedList(fat.buildChain(header.getFirstDirectorySectorLocation()));
        this.streamReader = streamReader;
        readDirectoryEntryCount();
    }

    private void readDirectoryEntryCount() {
        if(!sectorChain.isEmpty()) {
            directoryEntryCount = (sectorChain.size() - 1) * 4;
            Sector lastDirectoryEntrySector = sectors.sector(sectorChain.get(sectorChain.size() - 1));
            int directoriesInSector;
            for (directoriesInSector = 4; directoriesInSector > 0; directoriesInSector--) {
                int sectorStart = (directoriesInSector - 1) * 128;
                if(!Arrays.equals(UTF16_TERMINATING_BYTES, lastDirectoryEntrySector.subView(sectorStart, sectorStart + 2).getData())) {
                    break;
                }
            }
            directoryEntryCount += directoriesInSector;
        }
    }

    public DirectoryEntry getEntryById(int i) {
        int sectorNumber = i / 4;
        int shiftInsideSector = i % 4 * 128;
        return new DirectoryEntry(i, this, sectors.sector(sectorChain.get(sectorNumber)).subView(shiftInsideSector, shiftInsideSector + 128), streamReader);
    }

    public RootStorageDirectoryEntry createRootStorage() {
        if(directoryEntryCount != 0) {
            throw new IllegalStateException("Root Storage should be the first Directory Entry");
        }
        return new RootStorageDirectoryEntry(0, this, getViewForDirectoryEntry(), streamReader);
    }

    public StorageDirectoryEntry createStorage(String name, DirectoryEntry.ColorFlag colorFlag) {
        return new StorageDirectoryEntry(directoryEntryCount - 1, name, colorFlag, this, getViewForDirectoryEntry(), streamReader);
    }

    public DirectoryEntry createStream(String name, DirectoryEntry.ColorFlag colorFlag, InputStream data) {
        DirectoryEntry streamEntry = new DirectoryEntry(directoryEntryCount - 1, name, colorFlag, DirectoryEntry.ObjectType.Stream, this, getViewForDirectoryEntry(), streamReader);
        return streamEntry;
    }

    private DataView getViewForDirectoryEntry() {
        int directoriesRegisteredInCurrentSector = directoryEntryCount % 4;
        try {
            if (directoriesRegisteredInCurrentSector == 0) {
                Sector directoryEntrySector = sectors.allocate();
                sectorChain.add(directoryEntrySector.getPosition());
                header.setFirstDirectorySectorLocation(directoryEntrySector.getPosition());
                return directoryEntrySector.subView(0, 128);
            } else {
                return sectors.sector(sectorChain.getLast())
                        .subView(directoriesRegisteredInCurrentSector * DirectoryEntry.ENTRY_LENGTH, (directoriesRegisteredInCurrentSector + 1) * DirectoryEntry.ENTRY_LENGTH);
            }
        } finally {
            directoryEntryCount++;
        }
    }

}
