package com.ifedorov.cfbf;

import com.google.common.collect.Lists;
import com.ifedorov.cfbf.stream.StreamReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirectoryEntryChainTest {

    @Mock CompoundFile compoundFile;
    @Mock StreamReader streamReader;

    @Test
    void testGetDirectoryEntry() {
        Sector firstSector = mock(Sector.class);
        Sector secondSector = mock(Sector.class);
        when(secondSector.subView(0, DirectoryEntry.ENTRY_LENGTH)).thenReturn(DataView.from(DirectoryEntryTest.dummyDirectoryEntry()));
        when(firstSector.subView(DirectoryEntry.ENTRY_LENGTH * 3, DirectoryEntry.ENTRY_LENGTH * 4)).thenReturn(DataView.from(DirectoryEntryTest.dummyDirectoryEntry()));
        when(compoundFile.sector(0)).thenReturn(firstSector);
        when(compoundFile.sector(1)).thenReturn(secondSector);
        DirectoryEntryChain directoryEntryChain = new DirectoryEntryChain(compoundFile, Lists.newArrayList(0, 1), streamReader);
        directoryEntryChain.getEntryById(4);
        verify(secondSector, times(1)).subView(0, DirectoryEntry.ENTRY_LENGTH);
        directoryEntryChain.getEntryById(3);
        verify(firstSector, times(1)).subView(DirectoryEntry.ENTRY_LENGTH * 3, DirectoryEntry.ENTRY_LENGTH * 4);
    }
}