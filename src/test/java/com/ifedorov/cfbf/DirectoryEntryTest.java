package com.ifedorov.cfbf;

import com.google.common.base.VerifyException;
import com.ifedorov.cfbf.stream.StreamReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirectoryEntryTest {

    private static final byte[] DUMMY_DIRECTORY_ENTRY = new byte[DirectoryEntry.ENTRY_LENGTH];
    static {
        DUMMY_DIRECTORY_ENTRY[DirectoryEntry.FLAG_POSITION.OBJECT_TYPE] = (byte) DirectoryEntry.ObjectType.Storage.code();
        DUMMY_DIRECTORY_ENTRY[DirectoryEntry.FLAG_POSITION.COLOR_FLAG] = (byte) DirectoryEntry.ColorFlag.BLACK.code();
    }

    public static final byte[] dummyDirectoryEntry() {
        byte[] result = new byte[DirectoryEntry.ENTRY_LENGTH];
        System.arraycopy(DUMMY_DIRECTORY_ENTRY, 0, result, 0, DirectoryEntry.ENTRY_LENGTH);
        return result;
    }

    byte[] data;
    @Mock DirectoryEntryChain directoryEntryChain;
    @Mock StreamReader streamReader;

    @BeforeEach
    void init() {
        data = new byte[128];
        data[DirectoryEntry.FLAG_POSITION.OBJECT_TYPE] = (byte) DirectoryEntry.ObjectType.Storage.code();
        data[DirectoryEntry.FLAG_POSITION.COLOR_FLAG] = (byte) DirectoryEntry.ColorFlag.BLACK.code();
    }

    @Test
    void testDirectoryEntryShouldBe128BytesLong() {
        byte[] corruptedData = new byte[127];
        System.arraycopy(data, 0, corruptedData, 0, 127);
        assertThrows(VerifyException.class, ()->new DirectoryEntry(directoryEntryChain, DataView.from(corruptedData), streamReader));
    }

    @Test
    void testDirectoryEntryShouldHaveValidColorFlag() {
        data[DirectoryEntry.FLAG_POSITION.COLOR_FLAG] = (byte) DirectoryEntry.ColorFlag.BLACK.code();
        new DirectoryEntry(directoryEntryChain, DataView.from(data), streamReader);
        data[DirectoryEntry.FLAG_POSITION.COLOR_FLAG] = (byte) DirectoryEntry.ColorFlag.RED.code();
        new DirectoryEntry(directoryEntryChain, DataView.from(data), streamReader);
        data[DirectoryEntry.FLAG_POSITION.COLOR_FLAG] = (byte) 2;
        assertThrows(IllegalArgumentException.class, ()-> new DirectoryEntry(directoryEntryChain, DataView.from(data), streamReader));
        data[DirectoryEntry.FLAG_POSITION.COLOR_FLAG] = (byte) -1;
        assertThrows(IllegalArgumentException.class, ()-> new DirectoryEntry(directoryEntryChain, DataView.from(data), streamReader));
    }

    @Test
    void testDirectoryEntryShouldHaveValidObjectType() {
        System.arraycopy(Utils.toBytes(DirectoryEntry.ObjectType.Storage.code(), 1), 0, data, DirectoryEntry.FLAG_POSITION.OBJECT_TYPE, 1);
        new DirectoryEntry(directoryEntryChain, DataView.from(data), streamReader);
        System.arraycopy(Utils.toBytes(DirectoryEntry.ObjectType.RootStorage.code(), 1), 0, data, DirectoryEntry.FLAG_POSITION.OBJECT_TYPE, 1);
        new DirectoryEntry(directoryEntryChain, DataView.from(data), streamReader);
        System.arraycopy(Utils.toBytes(DirectoryEntry.ObjectType.Stream.code(), 1), 0, data, DirectoryEntry.FLAG_POSITION.OBJECT_TYPE, 1);
        new DirectoryEntry(directoryEntryChain, DataView.from(data), streamReader);
        System.arraycopy(Utils.toBytes(DirectoryEntry.ObjectType.Unknown.code(), 1), 0, data, DirectoryEntry.FLAG_POSITION.OBJECT_TYPE, 1);
        new DirectoryEntry(directoryEntryChain, DataView.from(data), streamReader);
        System.arraycopy(Utils.toBytes(-1, 1), 0, data, DirectoryEntry.FLAG_POSITION.OBJECT_TYPE, 1);
        assertThrows(IllegalArgumentException.class, ()->new DirectoryEntry(directoryEntryChain, DataView.from(data), streamReader));
    }

    @Test
    void testDirectoryEntryNameLength() {
        System.arraycopy(Utils.toBytes(65, 2), 0, data, DirectoryEntry.FLAG_POSITION.DIRECTORY_ENTRY_NAME_LENGTH, 2);
        assertThrows(VerifyException.class, () -> new DirectoryEntry(directoryEntryChain, DataView.from(data), streamReader));
        System.arraycopy(Utils.toBytes(15, 2), 0, data, DirectoryEntry.FLAG_POSITION.DIRECTORY_ENTRY_NAME_LENGTH, 2);
        assertEquals(15, new DirectoryEntry(directoryEntryChain, DataView.from(data), streamReader).getDirectoryEntryNameLength());
    }

    @Test
    void testGetChildAndSiblings() {
        DirectoryEntry child = mock(DirectoryEntry.class);
        DirectoryEntry leftSibling = mock(DirectoryEntry.class);
        DirectoryEntry rightSibling = mock(DirectoryEntry.class);
        when(directoryEntryChain.getEntryById(1)).thenReturn(child);
        when(directoryEntryChain.getEntryById(2)).thenReturn(leftSibling);
        when(directoryEntryChain.getEntryById(3)).thenReturn(rightSibling);
        System.arraycopy(Utils.toBytes(1, 4), 0, data, DirectoryEntry.FLAG_POSITION.CHILD, 4);
        System.arraycopy(Utils.toBytes(2, 4), 0, data, DirectoryEntry.FLAG_POSITION.LEFT_SIBLING, 4);
        System.arraycopy(Utils.toBytes(3, 4), 0, data, DirectoryEntry.FLAG_POSITION.RIGHT_SIBLING, 4);
        DirectoryEntry directoryEntry = new DirectoryEntry(directoryEntryChain, DataView.from(data), streamReader);
        assertEquals(child, directoryEntry.getChild().orElseThrow(() -> new RuntimeException("child has to be presented")));
        assertEquals(leftSibling, directoryEntry.getLeftSibling().orElseThrow(() -> new RuntimeException("child has to be presented")));
        assertEquals(rightSibling, directoryEntry.getRightSibling().orElseThrow(() -> new RuntimeException("child has to be presented")));
    }

    @Test
    void testNoChild() {
        System.arraycopy(Utils.FREESECT_MARK_OR_NOSTREAM, 0, data, DirectoryEntry.FLAG_POSITION.CHILD, 4);
        System.arraycopy(Utils.FREESECT_MARK_OR_NOSTREAM, 0, data, DirectoryEntry.FLAG_POSITION.LEFT_SIBLING, 4);
        System.arraycopy(Utils.FREESECT_MARK_OR_NOSTREAM, 0, data, DirectoryEntry.FLAG_POSITION.RIGHT_SIBLING, 4);
        DirectoryEntry directoryEntry = new DirectoryEntry(directoryEntryChain, DataView.from(data), streamReader);
        assertFalse(directoryEntry.getChild().isPresent());
        assertFalse(directoryEntry.getLeftSibling().isPresent());
        assertFalse(directoryEntry.getRightSibling().isPresent());
        verify(directoryEntryChain, times(0)).getEntryById(anyInt());
    }

}