package com.ifedorov.cfbf;

import com.ifedorov.cfbf.stream.StreamRW;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageDirectoryEntryTest {
    byte[] data;
    @Mock
    DirectoryEntryChain directoryEntryChain;
    @Mock
    StreamRW streamRW;

    @BeforeEach
    void init() {
        data = new byte[128];
        data[DirectoryEntry.FLAG_POSITION.OBJECT_TYPE] = (byte) DirectoryEntry.ObjectType.Storage.code();
        data[DirectoryEntry.FLAG_POSITION.COLOR_FLAG] = (byte) DirectoryEntry.ColorFlag.BLACK.code();
    }

    @Test
    public void testAddChildren() {

        StorageDirectoryEntry storage = new StorageDirectoryEntry(0, directoryEntryChain, DataView.from(data), streamRW);
        StorageDirectoryEntry child1 = new StorageDirectoryEntry(1, directoryEntryChain, DataView.from(Utils.copy(data)), streamRW);
        child1.setDirectoryEntryName("a");
        DirectoryEntry child2 = new DirectoryEntry(2, directoryEntryChain, DataView.from(Utils.copy(data)), streamRW);
        child2.setDirectoryEntryName("ab");
        DirectoryEntry child3 = new DirectoryEntry(3, directoryEntryChain, DataView.from(Utils.copy(data)), streamRW);
        child3.setDirectoryEntryName("b");
        when(directoryEntryChain.getEntryById(1)).thenReturn(child1);
        when(directoryEntryChain.getEntryById(2)).thenReturn(child2);
        when(directoryEntryChain.getEntryById(3)).thenReturn(child3);
        storage.addChild(child1);
        storage.addChild(child2);
        storage.addChild(child3);
        assertEquals("b", storage.getChild().get().getDirectoryEntryName());
        assertEquals("a", storage.getChild().get().getLeftSibling().get().getDirectoryEntryName());
        assertEquals("ab", storage.getChild().get().getRightSibling().get().getDirectoryEntryName());
        assertEquals(DirectoryEntry.ColorFlag.BLACK, storage.getChild().get().getColorFlag());
        assertEquals(DirectoryEntry.ColorFlag.RED, storage.getChild().get().getLeftSibling().get().getColorFlag());
        assertEquals(DirectoryEntry.ColorFlag.RED, storage.getChild().get().getRightSibling().get().getColorFlag());
    }
}