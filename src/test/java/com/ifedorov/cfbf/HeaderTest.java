package com.ifedorov.cfbf;

import com.google.common.base.VerifyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeaderTest {

    byte[] data;

    @BeforeEach
    void init() {
        data = new byte[512];
        System.arraycopy(Header.MAJOR_VERSION_3, 0, data, 26, 2);
        System.arraycopy(Header.BYTE_ORDER_LITTLE_ENDIAN, 0, data, 28, 2);
        System.arraycopy(Header.SECTOR_SHIFT_VERSION_3, 0, data, 30, 2);
        System.arraycopy(Header.MINI_SECTOR_SHIFT_VERSION_3, 0, data, 32, 2);
        System.arraycopy(Header.MINI_STREAM_CUTOFF_SIZE, 0, data, 56, 4);
    }

    @Test
    void testEnforceSize() {
        assertThrows(IndexOutOfBoundsException.class, () -> new Header(DataView.from(new byte[513])));
    }

    @Test
    void testSupportsOnlyMajorVersion3() {
        System.arraycopy(Utils.toBytes(0x0004, 2), 0, data, 26, 2);
        assertThrows(VerifyException.class, () -> new Header(DataView.from(data)));
    }

    @Test
    void testSupportsOnlyLittleEndianByteOrder() {
        System.arraycopy(Utils.toBytes(0xfffd, 2), 0, data, 28, 2);
        assertThrows(VerifyException.class, () -> new Header(DataView.from(data)));
    }

    @Test
    void testSupportsOnly512BytesSectorShift() {
        System.arraycopy(Utils.toBytes(0x0090, 2), 0, data, 30, 2);
        assertThrows(VerifyException.class, () -> new Header(DataView.from(data)));
    }

    @Test
    void testSupportsOnly64BytesMiniSectorShift() {
        System.arraycopy(Utils.toBytes(0x0007, 2), 0, data, 32, 2);
        assertThrows(VerifyException.class, () -> new Header(DataView.from(data)));
    }

    @Test
    void testEnforcesReservedBytes() {
        System.arraycopy(Utils.toBytes(0xddddddddddddl, 6), 0, data, 34, 6);
        assertThrows(VerifyException.class, () -> new Header(DataView.from(data)));
    }

    @Test
    void testEnforcesDirectoryServicesNumber() {
        System.arraycopy(Utils.toBytes(0x11111111, 4), 0, data, 40, 4);
        assertThrows(VerifyException.class, () -> new Header(DataView.from(data)));
    }

    @Test
    void testSupportsOnly4096BytesMiniStreamCutoffSize() {
        System.arraycopy(Utils.toBytes(0x00002000, 4), 0, data, 56, 4);
        assertThrows(VerifyException.class, () -> new Header(DataView.from(data)));
    }

}