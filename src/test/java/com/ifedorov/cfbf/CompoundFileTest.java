package com.ifedorov.cfbf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompoundFileTest {

    @Test
    void newCompoundFile() {
        CompoundFile compoundFile = new CompoundFile();
        DirectoryEntry rootStorage = compoundFile.getRootStorage();
        assertFalse(rootStorage.getLeftSibling().isPresent());
        assertFalse(rootStorage.getRightSibling().isPresent());
        assertFalse(rootStorage.getChild().isPresent());
        assertEquals(RootStorageDirectoryEntry.NAME, rootStorage.getDirectoryEntryName());
        assertEquals(10, rootStorage.getDirectoryEntryNameLengthUTF8());
    }

    @Test
    void testCopy() {
        CompoundFile compoundFile = new CompoundFile();
        RootStorageDirectoryEntry rootStorage = compoundFile.getRootStorage();
        rootStorage.addStorage("storage1");
        rootStorage.addStorage("storage2");
        rootStorage.addStream("stream1", new byte[]{1,2,3,4,5});
        CompoundFile copy = compoundFile.copy();
        assertTrue(copy.getRootStorage().children().anyMatch((directoryEntry -> "storage1".equals(directoryEntry.getDirectoryEntryName()))));
        assertTrue(copy.getRootStorage().children().anyMatch((directoryEntry -> "storage2".equals(directoryEntry.getDirectoryEntryName()))));
        assertTrue(copy.getRootStorage().children().anyMatch((directoryEntry -> "stream1".equals(directoryEntry.getDirectoryEntryName()))));
    }

}