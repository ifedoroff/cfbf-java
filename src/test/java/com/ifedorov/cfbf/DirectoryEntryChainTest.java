package com.ifedorov.cfbf;

import com.google.common.collect.Lists;
import com.ifedorov.cfbf.alloc.FAT;
import com.ifedorov.cfbf.stream.StreamRW;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirectoryEntryChainTest {

    @Mock Sectors sectors;
    @Mock StreamRW streamRW;
    @Mock FAT fat;
    @Mock Header header;

    @Test
    void testGetDirectoryEntry() {
        Sector firstSector = Sector.from(DataView.from(new byte[512]), 0);
        when(sectors.allocate()).thenReturn(firstSector);
        when(sectors.sector(0)).thenReturn(firstSector);
        when(fat.buildChain(anyInt())).thenReturn(Lists.newArrayList());
        when(header.getFirstDirectorySectorLocation()).thenReturn(Utils.ENDOFCHAIN_MARK_INT);
        DirectoryEntryChain directoryEntryChain = new DirectoryEntryChain(sectors, fat, header, streamRW);
        RootStorageDirectoryEntry rootStorage = directoryEntryChain.createRootStorage();
        StorageDirectoryEntry first_after_the_root = directoryEntryChain.createStorage("first after the root", DirectoryEntry.ColorFlag.RED);
        StorageDirectoryEntry second_after_the_root = directoryEntryChain.createStorage("second after the root", DirectoryEntry.ColorFlag.BLACK);
        assertEquals("Root", rootStorage.getDirectoryEntryName());
        assertEquals("first after the root", first_after_the_root.getDirectoryEntryName());
        assertEquals(DirectoryEntry.ColorFlag.RED, first_after_the_root.getColorFlag());
        assertEquals("second after the root", second_after_the_root.getDirectoryEntryName());
        assertEquals(DirectoryEntry.ColorFlag.BLACK, second_after_the_root.getColorFlag());
    }

    @Test
    void testCreateRootStorageDirectory() {
        Sector firstSector = Sector.from(DataView.from(new byte[512]), 0);
        when(sectors.allocate()).thenReturn(firstSector);
        when(fat.buildChain(anyInt()))
                .thenReturn(Lists.newArrayList());
        DirectoryEntryChain directoryEntryChain = new DirectoryEntryChain(sectors, fat, header, streamRW);
        RootStorageDirectoryEntry rootStorage = directoryEntryChain.createRootStorage();
        assertEquals("Root", rootStorage.getDirectoryEntryName());
        assertEquals(0, rootStorage.getId());
        verify(sectors, times(1)).allocate();
        verify(header, times(1)).setFirstDirectorySectorLocation(0);
    }

    @Test
    void testCreateStorageDirectory() {
        Sector firstSector = Sector.from(DataView.from(new byte[512]), 1);
        firstSector.subView(0, 256).writeAt(0, Utils.initializedWith(256, 1));
        when(sectors.sector(1)).thenReturn(firstSector);
        when(fat.buildChain(anyInt()))
                .thenReturn(Lists.newArrayList(0, 1));
        DirectoryEntryChain directoryEntryChain = new DirectoryEntryChain(sectors, fat, header, streamRW);
        StorageDirectoryEntry storage = directoryEntryChain.createStorage("storage", DirectoryEntry.ColorFlag.RED);
        assertEquals("storage", storage.getDirectoryEntryName());
        assertEquals(DirectoryEntry.ObjectType.Storage, storage.getObjectType());
        assertArrayEquals(storage.view.getData(), firstSector.subView(256, 384).getData());
    }

    @Test
    void testCreateStorageDirectoryWithNewSectorAllocation() {
        Sector zeroSector = Sector.from(DataView.from(new byte[512]), 0);
        Sector firstSector = Sector.from(DataView.from(new byte[512]), 1);
        zeroSector.subView(0, 512).writeAt(0, Utils.initializedWith(512, 1));
        when(sectors.sector(0)).thenReturn(zeroSector);
        when(sectors.allocate()).thenReturn(firstSector);
        when(fat.buildChain(anyInt()))
                .thenReturn(Lists.newArrayList(0));
        DirectoryEntryChain directoryEntryChain = new DirectoryEntryChain(sectors, fat, header, streamRW);
        StorageDirectoryEntry storage = directoryEntryChain.createStorage("storage", DirectoryEntry.ColorFlag.RED);
        verify(sectors, times(1)).allocate();
        assertEquals("storage", storage.getDirectoryEntryName());
        assertEquals(DirectoryEntry.ObjectType.Storage, storage.getObjectType());
        assertArrayEquals(storage.view.getData(), firstSector.subView(0, 128).getData());
    }
}