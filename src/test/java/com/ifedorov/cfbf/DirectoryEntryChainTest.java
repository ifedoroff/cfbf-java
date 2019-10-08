package com.ifedorov.cfbf;

import com.google.common.collect.Lists;
import com.ifedorov.cfbf.alloc.FAT;
import com.ifedorov.cfbf.stream.StreamRW;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirectoryEntryChainTest {

    @Mock Sectors sectors;
    @Mock StreamRW streamRW;
    @Mock FAT fat;
    @Mock Header header;

    @Test
    void testGetDirectoryEntry() {
        Sector firstSector = mock(Sector.class);
        Sector secondSector = mock(Sector.class);
        when(secondSector.subView(0, DirectoryEntry.ENTRY_LENGTH)).thenReturn(DataView.from(DirectoryEntryTest.dummyDirectoryEntry()));
        when(firstSector.subView(DirectoryEntry.ENTRY_LENGTH * 3, DirectoryEntry.ENTRY_LENGTH * 4)).thenReturn(DataView.from(DirectoryEntryTest.dummyDirectoryEntry()));
        when(sectors.sector(0)).thenReturn(firstSector);
        when(sectors.sector(1)).thenReturn(secondSector);
        when(header.getFirstDirectorySectorLocation()).thenReturn(0);
        when(fat.buildChain(0)).thenReturn(Lists.newArrayList(0,1));
        DirectoryEntryChain directoryEntryChain = new DirectoryEntryChain(sectors, fat, header, streamRW);
        directoryEntryChain.getEntryById(4);
        verify(secondSector, times(1)).subView(0, DirectoryEntry.ENTRY_LENGTH);
        directoryEntryChain.getEntryById(3);
        verify(firstSector, times(1)).subView(DirectoryEntry.ENTRY_LENGTH * 3, DirectoryEntry.ENTRY_LENGTH * 4);
    }
}