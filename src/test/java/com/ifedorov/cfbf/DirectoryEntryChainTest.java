package com.ifedorov.cfbf;

import com.google.common.collect.Lists;
import com.ifedorov.cfbf.alloc.FAT;
import com.ifedorov.cfbf.alloc.FATtoDIFATFacade;
import com.ifedorov.cfbf.stream.StreamHolder;
import com.ifedorov.cfbf.stream.StreamRW;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirectoryEntryChainTest {

    @Mock Sectors sectors;
    @Mock StreamHolder streamHolder;
    @Mock FAT fat;
    @Mock Header header;
    @Mock FATtoDIFATFacade faTtoDIFATFacade;

    @Test
    void testGetDirectoryEntry() {
        DataView rootView = DataView.empty();
        Header header = Header.empty(rootView.allocate(Header.HEADER_LENGTH));
        Sectors sectors = new Sectors(rootView, header);
        FAT fat = new FAT(sectors, header, faTtoDIFATFacade);
        DirectoryEntryChain directoryEntryChain = new DirectoryEntryChain(sectors, fat, header, streamHolder);
        RootStorageDirectoryEntry rootStorage = directoryEntryChain.createRootStorage();
        StorageDirectoryEntry first_after_the_root = directoryEntryChain.createStorage("first after the root", DirectoryEntry.ColorFlag.RED);
        StorageDirectoryEntry second_after_the_root = directoryEntryChain.createStorage("second after the root", DirectoryEntry.ColorFlag.BLACK);
        assertEquals(RootStorageDirectoryEntry.NAME, rootStorage.getDirectoryEntryName());
        assertEquals("first after the root", first_after_the_root.getDirectoryEntryName());
        assertEquals(DirectoryEntry.ColorFlag.RED, first_after_the_root.getColorFlag());
        assertEquals("second after the root", second_after_the_root.getDirectoryEntryName());
        assertEquals(DirectoryEntry.ColorFlag.BLACK, second_after_the_root.getColorFlag());
    }

    @Test
    void testShouldThrowExceptionIfNoSuchDirectoryEntryExists() {
        when(fat.buildChain(anyInt())).thenReturn(Lists.newArrayList());
        when(header.getFirstDirectorySectorLocation()).thenReturn(Utils.ENDOFCHAIN_MARK_INT);
        DirectoryEntryChain directoryEntryChain = new DirectoryEntryChain(sectors, fat, header, streamHolder);
        assertThrows(NoSuchElementException.class, () -> directoryEntryChain.getEntryById(0));
        assertThrows(NoSuchElementException.class, () -> directoryEntryChain.getEntryById(-1));
    }

    @Test
    void testCreateRootStorageDirectory() {
        DataView rootView = DataView.empty();
        Header header = Header.empty(rootView.allocate(Header.HEADER_LENGTH));
        Sectors sectors = new Sectors(rootView, header);
        FAT fat = new FAT(sectors, header, faTtoDIFATFacade);
        DirectoryEntryChain directoryEntryChain = new DirectoryEntryChain(sectors, fat, header, streamHolder);
        RootStorageDirectoryEntry rootStorage = directoryEntryChain.createRootStorage();
        assertEquals("Root Entry", rootStorage.getDirectoryEntryName());
        assertEquals(0, rootStorage.getId());
        int firstDirectorySectorLocation = header.getFirstDirectorySectorLocation();
        assertTrue(firstDirectorySectorLocation >= 0);
        assertEquals(1, fat.buildChain(firstDirectorySectorLocation).size());
    }

    @Test
    void testCreateStorageDirectory() {
        Sector zeroSector = Sector.from(DataView.from(new byte[512]), 1);
        Sector firstSector = Sector.from(DataView.from(new byte[512]), 1);
        DirectoryEntry.setChild(zeroSector.subView(0, 128), 1);
        DirectoryEntry.setChild(zeroSector.subView(128, 256), 2);
        DirectoryEntry.setChild(zeroSector.subView(256, 384), 3);
        DirectoryEntry.setChild(zeroSector.subView(384, 512), 4);
        DirectoryEntry.setChild(zeroSector.subView(0, 128), 5);
        DirectoryEntry.setChild(zeroSector.subView(128, 256), 6);
        firstSector.subView(0, 256).writeAt(0, Utils.initializedWith(256, 1));
        when(sectors.sector(0)).thenReturn(zeroSector);
        when(sectors.sector(1)).thenReturn(firstSector);
        when(fat.buildChain(anyInt()))
                .thenReturn(Lists.newArrayList(0, 1));
        DirectoryEntryChain directoryEntryChain = new DirectoryEntryChain(sectors, fat, header, streamHolder);
        StorageDirectoryEntry storage = directoryEntryChain.createStorage("storage", DirectoryEntry.ColorFlag.RED);
        assertEquals("storage", storage.getDirectoryEntryName());
        assertEquals(DirectoryEntry.ObjectType.Storage, storage.getObjectType());
        assertArrayEquals(storage.view.getData(), firstSector.subView(256, 384).getData());
    }

    @Test
    void testCreateStorageDirectoryWithNewSectorAllocation() {
        Sector zeroSector = Sector.from(DataView.from(new byte[512]), 0);
        DirectoryEntry.setChild(zeroSector.subView(0, 128), 1);
        DirectoryEntry.setChild(zeroSector.subView(128, 256), 2);
        DirectoryEntry.setChild(zeroSector.subView(256, 384), 3);
        DirectoryEntry.setLeftSibling(zeroSector.subView(384, 512), Utils.FREESECT_MARK_OR_NOSTREAM_INT);
        Sector firstSector = Sector.from(DataView.from(new byte[512]), 1);
//        zeroSector.subView(0, 512).writeAt(0, Utils.initializedWith(512, 1));
        when(sectors.sector(0)).thenReturn(zeroSector);
        when(sectors.allocate()).thenReturn(firstSector);
        when(fat.buildChain(anyInt()))
                .thenReturn(Lists.newArrayList(0));
        DirectoryEntryChain directoryEntryChain = new DirectoryEntryChain(sectors, fat, header, streamHolder);
        StorageDirectoryEntry storage = directoryEntryChain.createStorage("storage", DirectoryEntry.ColorFlag.RED);
        verify(sectors, times(1)).allocate();
        assertEquals("storage", storage.getDirectoryEntryName());
        assertEquals(DirectoryEntry.ObjectType.Storage, storage.getObjectType());
        assertArrayEquals(storage.view.getData(), firstSector.subView(0, 128).getData());
    }

    @Test
    void testCreateSeveralDirectories() {
        DataView rootView = DataView.empty();
        Header header = Header.empty(rootView.allocate(Header.HEADER_LENGTH));
        Sectors sectors = new Sectors(rootView, header);
        FAT fat = spy(new FAT(sectors, header, faTtoDIFATFacade));
        DirectoryEntryChain directoryEntryChain = new DirectoryEntryChain(sectors, fat, header, streamHolder);
        directoryEntryChain.createRootStorage();
        IntStream.range(0, 10).forEach((i) -> directoryEntryChain.createStorage("storage" + i, DirectoryEntry.ColorFlag.BLACK));
        int firstDirectorySectorLocation = header.getFirstDirectorySectorLocation();
        assertTrue(firstDirectorySectorLocation >= 0);
        assertEquals(3, fat.buildChain(firstDirectorySectorLocation).size());
        IntStream.range(0, 10).forEach(i -> assertEquals("storage" + i, directoryEntryChain.getEntryById(i + 1).getDirectoryEntryName()));
    }

}