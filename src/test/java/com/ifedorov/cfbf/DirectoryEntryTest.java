package com.ifedorov.cfbf;

import com.google.common.base.VerifyException;
import com.ifedorov.cfbf.alloc.FAT;
import com.ifedorov.cfbf.alloc.FATtoDIFATFacade;
import com.ifedorov.cfbf.stream.RegularStreamRW;
import com.ifedorov.cfbf.stream.StreamHolder;
import com.ifedorov.cfbf.stream.StreamRW;
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

    public static byte[] dummyDirectoryEntry() {
        return Utils.copy(DUMMY_DIRECTORY_ENTRY);
    }

    byte[] data;
    @Mock DirectoryEntryChain directoryEntryChain;
    @Mock StreamRW streamRW;
    @Mock
    FATtoDIFATFacade faTtoDIFATFacade;

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
        assertThrows(VerifyException.class, ()->new DirectoryEntry(0, directoryEntryChain, new DataView.SimpleDataView(corruptedData)));
    }

    @Test
    void testDirectoryEntryShouldHaveValidColorFlag() {
        data[DirectoryEntry.FLAG_POSITION.COLOR_FLAG] = (byte) DirectoryEntry.ColorFlag.BLACK.code();
        new DirectoryEntry(0, directoryEntryChain, new DataView.SimpleDataView(data));
        data[DirectoryEntry.FLAG_POSITION.COLOR_FLAG] = (byte) DirectoryEntry.ColorFlag.RED.code();
        new DirectoryEntry(1, directoryEntryChain, new DataView.SimpleDataView(data));
        data[DirectoryEntry.FLAG_POSITION.COLOR_FLAG] = (byte) 2;
        assertThrows(IllegalArgumentException.class, ()-> new DirectoryEntry(0, directoryEntryChain, new DataView.SimpleDataView(data)));
        data[DirectoryEntry.FLAG_POSITION.COLOR_FLAG] = (byte) -1;
        assertThrows(IllegalArgumentException.class, ()-> new DirectoryEntry(1, directoryEntryChain, new DataView.SimpleDataView(data)));
    }

    @Test
    void testDirectoryEntryShouldHaveValidObjectType() {
        System.arraycopy(Utils.toBytesLE(DirectoryEntry.ObjectType.Storage.code(), 1), 0, data, DirectoryEntry.FLAG_POSITION.OBJECT_TYPE, 1);
        new DirectoryEntry(0, directoryEntryChain, new DataView.SimpleDataView(data));
        System.arraycopy(Utils.toBytesLE(DirectoryEntry.ObjectType.RootStorage.code(), 1), 0, data, DirectoryEntry.FLAG_POSITION.OBJECT_TYPE, 1);
        new DirectoryEntry(1, directoryEntryChain, new DataView.SimpleDataView(data));
        System.arraycopy(Utils.toBytesLE(DirectoryEntry.ObjectType.Stream.code(), 1), 0, data, DirectoryEntry.FLAG_POSITION.OBJECT_TYPE, 1);
        new DirectoryEntry(2, directoryEntryChain, new DataView.SimpleDataView(data));
        System.arraycopy(Utils.toBytesLE(DirectoryEntry.ObjectType.Unknown.code(), 1), 0, data, DirectoryEntry.FLAG_POSITION.OBJECT_TYPE, 1);
        new DirectoryEntry(3, directoryEntryChain, new DataView.SimpleDataView(data));
        System.arraycopy(Utils.toBytesLE(-1, 1), 0, data, DirectoryEntry.FLAG_POSITION.OBJECT_TYPE, 1);
        assertThrows(IllegalArgumentException.class, ()->new DirectoryEntry(4, directoryEntryChain, new DataView.SimpleDataView(data)));
    }

    @Test
    void testDirectoryEntryNameLength() {
        System.arraycopy(Utils.toBytesLE(65, 2), 0, data, DirectoryEntry.FLAG_POSITION.DIRECTORY_ENTRY_NAME_LENGTH, 2);
        assertThrows(VerifyException.class, () -> new DirectoryEntry(0, directoryEntryChain, new DataView.SimpleDataView(data)));
        System.arraycopy(Utils.toBytesLE(15, 2), 0, data, DirectoryEntry.FLAG_POSITION.DIRECTORY_ENTRY_NAME_LENGTH, 2);
        assertEquals(15, new DirectoryEntry(1, directoryEntryChain, new DataView.SimpleDataView(data)).getDirectoryEntryNameLength());
    }

    @Test
    void testGetChildAndSiblings() {
        DirectoryEntry child = mock(DirectoryEntry.class);
        DirectoryEntry leftSibling = mock(DirectoryEntry.class);
        DirectoryEntry rightSibling = mock(DirectoryEntry.class);
        when(directoryEntryChain.getEntryById(1)).thenReturn(child);
        when(directoryEntryChain.getEntryById(2)).thenReturn(leftSibling);
        when(directoryEntryChain.getEntryById(3)).thenReturn(rightSibling);
        System.arraycopy(Utils.toBytesLE(1, 4), 0, data, DirectoryEntry.FLAG_POSITION.CHILD, 4);
        System.arraycopy(Utils.toBytesLE(2, 4), 0, data, DirectoryEntry.FLAG_POSITION.LEFT_SIBLING, 4);
        System.arraycopy(Utils.toBytesLE(3, 4), 0, data, DirectoryEntry.FLAG_POSITION.RIGHT_SIBLING, 4);
        DirectoryEntry directoryEntry = new DirectoryEntry(0, directoryEntryChain, new DataView.SimpleDataView(data));
        assertEquals(child, directoryEntry.getChild().orElseThrow(() -> new RuntimeException("child has to be presented")));
        assertEquals(leftSibling, directoryEntry.getLeftSibling().orElseThrow(() -> new RuntimeException("child has to be presented")));
        assertEquals(rightSibling, directoryEntry.getRightSibling().orElseThrow(() -> new RuntimeException("child has to be presented")));
    }

    @Test
    void testNoChild() {
        System.arraycopy(Utils.FREESECT_MARK_OR_NOSTREAM, 0, data, DirectoryEntry.FLAG_POSITION.CHILD, 4);
        System.arraycopy(Utils.FREESECT_MARK_OR_NOSTREAM, 0, data, DirectoryEntry.FLAG_POSITION.LEFT_SIBLING, 4);
        System.arraycopy(Utils.FREESECT_MARK_OR_NOSTREAM, 0, data, DirectoryEntry.FLAG_POSITION.RIGHT_SIBLING, 4);
        DirectoryEntry directoryEntry = new DirectoryEntry(0, directoryEntryChain, new DataView.SimpleDataView(data));
        assertFalse(directoryEntry.getChild().isPresent());
        assertFalse(directoryEntry.getLeftSibling().isPresent());
        assertFalse(directoryEntry.getRightSibling().isPresent());
        verify(directoryEntryChain, times(0)).getEntryById(anyInt());
    }

    @Test
    void testSetSiblings() {
        DirectoryEntry directoryEntry = new DirectoryEntry(0, directoryEntryChain, new DataView.SimpleDataView(data));
        DirectoryEntry rightSibling = new DirectoryEntry(1, directoryEntryChain, new DataView.SimpleDataView(Utils.copy(data)));
        DirectoryEntry leftSibling = new DirectoryEntry(2, directoryEntryChain, new DataView.SimpleDataView(Utils.copy(data)));
        when(directoryEntryChain.getEntryById(1)).thenReturn(rightSibling);
        when(directoryEntryChain.getEntryById(2)).thenReturn(leftSibling);
        directoryEntry.setRightSibling(rightSibling);
        directoryEntry.setLeftSibling(leftSibling);
        assertEquals(1, directoryEntry.getRightSibling().get().getId());
        assertEquals(2, directoryEntry.getLeftSibling().get().getId());
        directoryEntry.setRightSibling(null);
        directoryEntry.setLeftSibling(null);
        assertFalse(directoryEntry.getRightSibling().isPresent());
        assertFalse(directoryEntry.getLeftSibling().isPresent());
    }

    @Test
    void testCompareDirectoryEntries() {
        DirectoryEntry one = new DirectoryEntry(0, directoryEntryChain, new DataView.SimpleDataView(data));
        DirectoryEntry two = new DirectoryEntry(1, directoryEntryChain, new DataView.SimpleDataView(Utils.copy(data)));
        one.setDirectoryEntryName("a");
        two.setDirectoryEntryName("b");
        assertEquals(-1, one.compareTo(two));
        one.setDirectoryEntryName("a");
        two.setDirectoryEntryName("ab");
        assertEquals(-1, one.compareTo(two));
        one.setDirectoryEntryName("a");
        two.setDirectoryEntryName("A");
        assertEquals(0, one.compareTo(two));
    }

    @Test
    void testSetDirectoryEntryName() {
        DirectoryEntry directoryEntry = new DirectoryEntry(0, directoryEntryChain, new DataView.SimpleDataView(data));
        directoryEntry.setDirectoryEntryName("a");
        assertEquals("a", directoryEntry.getDirectoryEntryName());
        assertEquals(4, directoryEntry.getDirectoryEntryNameLength());
        directoryEntry.setDirectoryEntryName("abc");
        assertEquals("abc", directoryEntry.getDirectoryEntryName());
        assertEquals(8, directoryEntry.getDirectoryEntryNameLength());
        String string31Chars = "1234567891234567891234567891234";
        String string32Chars = "12345678912345678912345678912345";
        assertThrows(IllegalArgumentException.class, () -> directoryEntry.setDirectoryEntryName(string32Chars));
        assertDoesNotThrow(() -> directoryEntry.setDirectoryEntryName(string31Chars));
        assertEquals(string31Chars, directoryEntry.getDirectoryEntryName());
        assertEquals(64, directoryEntry.getDirectoryEntryNameLength());
    }

    @Test
    void testCreateNewDirectoryEntry() {
        DataView rootView = DataView.empty();
        Header header = Header.empty(rootView.allocate(Header.HEADER_LENGTH));
        Sectors sectors = new Sectors(rootView, header);
        FAT fat = new FAT(sectors, header, faTtoDIFATFacade);
        RegularStreamRW regularStreamRW = new RegularStreamRW(fat, sectors, header);
        DirectoryEntryChain directoryEntryChain = new DirectoryEntryChain(sectors, fat, header, new StreamHolder(regularStreamRW, null, 2000));
        StreamDirectoryEntry stream = directoryEntryChain.createStream("1", DirectoryEntry.ColorFlag.BLACK, Utils.initializedWith(2000, 1));
        assertFalse(stream.getLeftSibling().isPresent());
        assertFalse(stream.getRightSibling().isPresent());
        assertArrayEquals(Utils.initializedWith(2000, 1), stream.getStreamData());
        assertEquals(2000, stream.getStreamSize());
    }
}