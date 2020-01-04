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
        assertEquals(Utils.FREESECT_MARK_OR_NOSTREAM_INT ,rootStorage.getStreamSize());
        assertEquals(RootStorageDirectoryEntry.NAME, rootStorage.getDirectoryEntryName());
        assertEquals(10, rootStorage.getDirectoryEntryNameLengthUTF8());
    }

}