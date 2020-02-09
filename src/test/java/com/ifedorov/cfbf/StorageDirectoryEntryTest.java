package com.ifedorov.cfbf;

import com.ifedorov.cfbf.stream.StreamRW;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyByte;
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
        data = new byte[512];
        data[DirectoryEntry.FLAG_POSITION.OBJECT_TYPE] = (byte) DirectoryEntry.ObjectType.Storage.code();
        data[DirectoryEntry.FLAG_POSITION.COLOR_FLAG] = (byte) DirectoryEntry.ColorFlag.BLACK.code();
        System.arraycopy(Utils.FREESECT_MARK_OR_NOSTREAM, 0, data, DirectoryEntry.FLAG_POSITION.LEFT_SIBLING, 4);
        System.arraycopy(Utils.FREESECT_MARK_OR_NOSTREAM, 0, data, DirectoryEntry.FLAG_POSITION.RIGHT_SIBLING, 4);
        System.arraycopy(Utils.FREESECT_MARK_OR_NOSTREAM, 0, data, DirectoryEntry.FLAG_POSITION.CHILD, 4);
    }

    @Test
    public void testAddChildren() {

        RootStorageDirectoryEntry storage = new RootStorageDirectoryEntry(0, directoryEntryChain, DataView.from(Utils.copy(data)).subView(0, 128), streamRW);
        DirectoryEntry child1 = new DirectoryEntry(1, "a", DirectoryEntry.ColorFlag.RED, DirectoryEntry.ObjectType.Stream, directoryEntryChain, DataView.from(Utils.copy(data)), streamRW);
        DirectoryEntry child2 = new DirectoryEntry(2, "ab", DirectoryEntry.ColorFlag.RED, DirectoryEntry.ObjectType.Stream, directoryEntryChain, DataView.from(Utils.copy(data)), streamRW);
        StorageDirectoryEntry child3 = new StorageDirectoryEntry(3, "b", DirectoryEntry.ColorFlag.RED, directoryEntryChain, DataView.from(Utils.copy(data)), streamRW);
        when(directoryEntryChain.createStream("a", DirectoryEntry.ColorFlag.RED, new byte[1])).thenReturn(child1);
        when(directoryEntryChain.createStream("ab", DirectoryEntry.ColorFlag.RED, new byte[1])).thenReturn(child2);
        when(directoryEntryChain.createStorage("b", DirectoryEntry.ColorFlag.RED)).thenReturn(child3);
        when(directoryEntryChain.getEntryById(1)).thenReturn(child1);
        when(directoryEntryChain.getEntryById(2)).thenReturn(child2);
        when(directoryEntryChain.getEntryById(3)).thenReturn(child3);
        storage.addStream("a", new byte[1]);
        storage.addStream("ab", new byte[1]);
        storage.addStorage("b");
        assertEquals("b", storage.getChild().get().getDirectoryEntryName());
        assertEquals("a", storage.getChild().get().getLeftSibling().get().getDirectoryEntryName());
        assertEquals("ab", storage.getChild().get().getRightSibling().get().getDirectoryEntryName());
        assertEquals(DirectoryEntry.ColorFlag.BLACK, storage.getChild().get().getColorFlag());
        assertEquals(DirectoryEntry.ColorFlag.RED, storage.getChild().get().getLeftSibling().get().getColorFlag());
        assertEquals(DirectoryEntry.ColorFlag.RED, storage.getChild().get().getRightSibling().get().getColorFlag());
    }

    @Test
    public void testFindChild() {
        CompoundFile compoundFile = new CompoundFile();
        RootStorageDirectoryEntry rootStorage = compoundFile.getRootStorage();
        StorageDirectoryEntry storage1 = rootStorage.addStorage("storage1");
        rootStorage.addStorage("storage2");
        rootStorage.addStream("stream1", new byte[]{1,2,3,4,5});
        storage1.addStorage("storage11");
        storage1.addStream("stream11", new byte[]{5,4,3,2,1});
        assertNotNull(rootStorage.findChild((directoryEntry -> directoryEntry.getDirectoryEntryName().equalsIgnoreCase("storage1"))));
        assertNotNull(rootStorage.findChild((directoryEntry -> directoryEntry.getDirectoryEntryName().equalsIgnoreCase("storage2"))));
        assertArrayEquals(new byte[]{1,2,3,4,5}, rootStorage.findChild((directoryEntry -> directoryEntry.getDirectoryEntryName().equalsIgnoreCase("stream1"))).getStreamData());
        assertArrayEquals(new byte[]{5,4,3,2,1}, storage1.findChild((directoryEntry -> directoryEntry.getDirectoryEntryName().equalsIgnoreCase("stream11"))).getStreamData());
    }

    @Test
    public void testFindChildren() {
        CompoundFile compoundFile = new CompoundFile();
        RootStorageDirectoryEntry rootStorage = compoundFile.getRootStorage();
        rootStorage.addStorage("storage1");
        rootStorage.addStorage("storage2");
        rootStorage.addStream("stream1", new byte[]{1,2,3,4,5});
        assertEquals(2, rootStorage.findChildren((directoryEntry -> directoryEntry instanceof StorageDirectoryEntry)).size());
        assertEquals(1, rootStorage.findChildren((directoryEntry -> directoryEntry instanceof StreamDirectoryEntry)).size());
    }
}