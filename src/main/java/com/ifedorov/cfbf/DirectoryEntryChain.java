package com.ifedorov.cfbf;

import com.google.common.collect.Lists;
import com.ifedorov.cfbf.alloc.FAT;
import com.ifedorov.cfbf.stream.StreamHolder;
import com.ifedorov.cfbf.stream.StreamRW;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import static com.ifedorov.cfbf.DirectoryEntry.UTF16_TERMINATING_BYTES;
import static com.ifedorov.cfbf.RootStorageDirectoryEntry.NAME;

public class DirectoryEntryChain {

    private Sectors sectors;
    private FAT fat;
    private Header header;
    private LinkedList<Integer> sectorChain;
    private StreamHolder streamHolder;
    private int directoryEntryCount;

    public DirectoryEntryChain(Sectors sectors, FAT fat, Header header, StreamHolder streamHolder) {
        this.sectors = sectors;
        this.fat = fat;
        this.header = header;
        this.sectorChain = Lists.newLinkedList(fat.buildChain(header.getFirstDirectorySectorLocation()));
        this.streamHolder = streamHolder;
        readDirectoryEntryCount();
    }

    private void readDirectoryEntryCount() {
        if(!sectorChain.isEmpty()) {
            int maxDirectoryEntryPosition = -1;
            for (Integer sectorPosition : sectorChain) {
                Sector sector = sectors.sector(sectorPosition);
                for (int i = 0; i < 4; i++) {
                    DataView directoryEntryView = sector.subView(i * 128, (i + 1) * 128);
                    int leftSiblingPosition = DirectoryEntry.getLeftSiblingPosition(directoryEntryView);
                    int rightSiblingPosition = DirectoryEntry.getRightSiblingPosition(directoryEntryView);
                    int childPosition = DirectoryEntry.getChildPosition(directoryEntryView);
                    maxDirectoryEntryPosition = Math.max(maxDirectoryEntryPosition, leftSiblingPosition);
                    maxDirectoryEntryPosition = Math.max(maxDirectoryEntryPosition, rightSiblingPosition);
                    maxDirectoryEntryPosition = Math.max(maxDirectoryEntryPosition, childPosition);
                }
            }
            directoryEntryCount = maxDirectoryEntryPosition + 1;
        }
    }

    public RootStorageDirectoryEntry getRootStorage() {
        return getEntryById(0);
    }

    public <T extends DirectoryEntry> T getEntryById(int i) {
        if(i < 0 || i > directoryEntryCount - 1) {
            throw new NoSuchElementException("" + i);
        }
        int sectorNumber = i / 4;
        int shiftInsideSector = i % 4 * 128;
        DataView view = sectors.sector(sectorChain.get(sectorNumber)).subView(shiftInsideSector, shiftInsideSector + 128);
        DirectoryEntry.ObjectType objectType = DirectoryEntry.ObjectType.fromCode(view.subView(DirectoryEntry.FLAG_POSITION.OBJECT_TYPE, DirectoryEntry.FLAG_POSITION.OBJECT_TYPE + 1).getData()[0]);
        if(objectType == DirectoryEntry.ObjectType.RootStorage) {
            return (T) new RootStorageDirectoryEntry(i, this, view);
        } else if(objectType == DirectoryEntry.ObjectType.Storage) {
            return (T) new StorageDirectoryEntry(i, this, view);
        } else {
            return (T) new StreamDirectoryEntry(i, this, view, streamHolder);
        }
    }

    public RootStorageDirectoryEntry createRootStorage() {
        if(directoryEntryCount != 0) {
            throw new IllegalStateException("Root Storage should be the first Directory Entry");
        }
        DataView view = getViewForDirectoryEntry();
        RootStorageDirectoryEntry rootStorageDirectoryEntry = new RootStorageDirectoryEntry.Builder(0, this, view)
                .build();
        return rootStorageDirectoryEntry;
    }

    public StorageDirectoryEntry createStorage(String name, DirectoryEntry.ColorFlag colorFlag) {
        return new StorageDirectoryEntry.Builder(directoryEntryCount, this, getViewForDirectoryEntry())
                .name(name)
                .color(colorFlag)
                .build();
    }

    public StreamDirectoryEntry createStream(String name, DirectoryEntry.ColorFlag colorFlag, byte[] data) {
        StreamDirectoryEntry streamEntry =
                new StreamDirectoryEntry.Builder(directoryEntryCount, this, getViewForDirectoryEntry(), streamHolder)
                .name(name)
                .color(colorFlag)
                .build();
        if(data.length > 0) {
            streamEntry.setStreamData(data);
        }
        return streamEntry;
    }

    private DataView getViewForDirectoryEntry() {
        int directoriesRegisteredInCurrentSector = directoryEntryCount % 4;
        try {
            if (directoriesRegisteredInCurrentSector == 0) {
                Sector directoryEntrySector = sectors.allocate();
                if(sectorChain.isEmpty()) {
                    header.setFirstDirectorySectorLocation(directoryEntrySector.getPosition());
                    fat.registerSector(directoryEntrySector.getPosition(), null);
                } else {
                    fat.registerSector(directoryEntrySector.getPosition(), sectorChain.getLast());
                }
                sectorChain.add(directoryEntrySector.getPosition());
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
