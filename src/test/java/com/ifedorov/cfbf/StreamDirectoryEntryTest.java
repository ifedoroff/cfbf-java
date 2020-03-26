package com.ifedorov.cfbf;

import com.google.common.base.VerifyException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StreamDirectoryEntryTest {

    @Test
    void testReadDataMiniStream() {
        CompoundFile compoundFile = new CompoundFile();
        StorageDirectoryEntry storage = compoundFile.getRootStorage().addStorage("test");
        StreamDirectoryEntry miniStream = storage.addStream("mini", new byte[]{1, 2, 3, 4, 5, 6, 7});
        assertArrayEquals(new byte[]{3,4,5}, miniStream.read(2, 5));


        byte[] bytes = Utils.initializedWith(65, 0);
        bytes[64] = 1;
        StreamDirectoryEntry miniStream1 = storage.addStream("mini1", bytes);
        assertArrayEquals(new byte[]{0, 1}, miniStream1.read(63, 65));
    }

    @Test
    void testReadDataRegularStream() {
        CompoundFile compoundFile = new CompoundFile();
        StorageDirectoryEntry storage = compoundFile.getRootStorage().addStorage("test");
        byte[] bytes = Utils.initializedWith(4096, 0);
        bytes[4093] = 2;
        bytes[4094] = 3;
        bytes[4095] = 4;
        StreamDirectoryEntry miniStream = storage.addStream("mini", bytes);
        assertArrayEquals(new byte[]{2,3,4}, miniStream.read(4093, 4096));


        bytes = Utils.initializedWith(4097, 0);
        bytes[4096] = 1;
        StreamDirectoryEntry miniStream1 = storage.addStream("mini1", bytes);
        assertArrayEquals(new byte[]{0, 1}, miniStream1.read(4095, 4097));
    }


    @Test
    void testWriteAtMiniStream() {
        CompoundFile compoundFile = new CompoundFile();
        StorageDirectoryEntry storage = compoundFile.getRootStorage().addStorage("test");
        StreamDirectoryEntry finalMiniStream = storage.addStream("mini", new byte[]{0, 1, 2, 3, 4, 5, 6, 7});
        assertThrows(VerifyException.class, () -> finalMiniStream.writeAt(0, Utils.initializedWith(9, 0)));
        assertThrows(VerifyException.class, () -> finalMiniStream.writeAt(9, Utils.initializedWith(1, 0)));
        assertThrows(VerifyException.class, () -> finalMiniStream.writeAt(-1, Utils.initializedWith(1, 0)));
        finalMiniStream.writeAt(2, new byte[]{15});
        assertArrayEquals(new byte[]{15}, finalMiniStream.read(2, 3));

        StreamDirectoryEntry miniStream1 = storage.addStream("mini1", Utils.initializedWith(65, 0));
        miniStream1.writeAt(63, new byte[]{1});
        assertArrayEquals(new byte[]{1}, miniStream1.read(63, 64));
        miniStream1.writeAt(64, new byte[]{1});
        assertArrayEquals(new byte[]{1}, miniStream1.read(64, 65));
    }

    @Test
    void testWriteAtRegularStream() {
        CompoundFile compoundFile = new CompoundFile();
        StorageDirectoryEntry storage = compoundFile.getRootStorage().addStorage("test");
        StreamDirectoryEntry finalMiniStream = storage.addStream("mini", new byte[]{0, 1, 2, 3, 4, 5, 6, 7});
        assertThrows(VerifyException.class, () -> finalMiniStream.writeAt(0, Utils.initializedWith(9, 0)));
        assertThrows(VerifyException.class, () -> finalMiniStream.writeAt(9, Utils.initializedWith(1, 0)));
        assertThrows(VerifyException.class, () -> finalMiniStream.writeAt(-1, Utils.initializedWith(1, 0)));
        byte[] bytes = Utils.initializedWith(4096, 0);
        finalMiniStream.setStreamData(bytes);
        assertThrows(VerifyException.class, () -> finalMiniStream.writeAt(0, Utils.initializedWith(4097, 0)));
        assertThrows(VerifyException.class, () -> finalMiniStream.writeAt(4096, Utils.initializedWith(1, 0)));
        assertThrows(VerifyException.class, () -> finalMiniStream.writeAt(-1, Utils.initializedWith(1, 0)));
        finalMiniStream.writeAt(2, new byte[]{15});
        assertArrayEquals(new byte[]{15}, finalMiniStream.read(2, 3));

        StreamDirectoryEntry miniStream1 = storage.addStream("mini1", Utils.initializedWith(4097, 0));
        miniStream1.writeAt(4095, new byte[]{1});
        assertArrayEquals(new byte[]{1}, miniStream1.read(4095, 4096));
        miniStream1.writeAt(4096, new byte[]{1});
        assertArrayEquals(new byte[]{1}, miniStream1.read(4096, 4097));
    }

    @Test
    void testAppendMiniStream() {
        CompoundFile compoundFile = new CompoundFile();
        StreamDirectoryEntry mini = compoundFile.getRootStorage().addStream("mini", new byte[]{0, 1, 2});
        mini.append(new byte[]{3,4,5});
        assertArrayEquals(new byte[]{0, 1,2,3,4,5}, mini.getStreamData());
        mini = compoundFile.getRootStorage().addStream("mini1", Utils.initializedWith(64, 1));
        mini.append(new byte[]{2,3,4});
        assertEquals(67, mini.getStreamSize());
        assertArrayEquals(new byte[]{2,3,4}, mini.read(64, 67));
        mini = compoundFile.getRootStorage().addStream("mini2", Utils.initializedWith(63, 1));
        mini.append(new byte[]{0});
        assertEquals(64, mini.getStreamSize());
        assertArrayEquals(new byte[]{0}, mini.read(63, 64));
    }

    @Test
    void testAppendRegularStream() {
        CompoundFile compoundFile = new CompoundFile();
        StreamDirectoryEntry mini = compoundFile.getRootStorage().addStream("mini", Utils.initializedWith(4100, 0));
        mini.append(new byte[]{3,4,5});
        assertEquals(4103, mini.getStreamSize());
        assertArrayEquals(new byte[]{3,4,5}, mini.read(4100, 4103));
        mini = compoundFile.getRootStorage().addStream("mini1", Utils.initializedWith(4096, 1));
        mini.append(new byte[]{2,3,4});
        assertEquals(4099, mini.getStreamSize());
        assertArrayEquals(new byte[]{2,3,4}, mini.read(4096, 4099));
        mini = compoundFile.getRootStorage().addStream("mini2", Utils.initializedWith(63, 1));
        mini.append(new byte[]{0});
        assertEquals(64, mini.getStreamSize());
        assertArrayEquals(new byte[]{0}, mini.read(63, 64));
    }

    @Test
    void testAppendWithMiniStreamChangeToRegularStream() {
        CompoundFile compoundFile = new CompoundFile();
        StreamDirectoryEntry miniStream = compoundFile.getRootStorage().addStream("mini", Utils.initializedWith(4095, 0));
        miniStream.append(new byte[]{1});
        //Previous operation should turn mini stream in a regular stream
        StreamDirectoryEntry regularStream = miniStream;
        assertEquals(4096, regularStream.getStreamData().length);
        assertArrayEquals(new byte[]{1}, regularStream.read(4095, 4096));
    }

}
