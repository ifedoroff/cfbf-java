package com.ifedorov.cfbf;

import com.google.common.base.Verify;
import com.google.common.base.VerifyException;

public class Header {

    public static final byte[] MAJOR_VERSION_3 = Utils.toBytes(0x0003, 2);
    public static final byte[] BYTE_ORDER_LITTLE_ENDIAN = Utils.toBytes(0xfffe, 2);
    public static final byte[] SECTOR_SHIFT_VERSION_3 = Utils.toBytes(0x0009, 2);
    public static final byte[] MINI_SECTOR_SHIFT_VERSION_3 = Utils.toBytes(0x0006, 2);
    public static final byte[] MINI_STREAM_CUTOFF_SIZE = Utils.toBytes(0x00001000, 4);

    public Header(DataView dataView) {
        if(dataView.getSize() > 512) {
            throw new IndexOutOfBoundsException();
        }
        Verify.verify(Header.MAJOR_VERSION_3 == dataView.subView(26, 28).getData());
        Verify.verify(Header.BYTE_ORDER_LITTLE_ENDIAN == dataView.subView(28, 30).getData());
        Verify.verify(Header.SECTOR_SHIFT_VERSION_3 == dataView.subView(30, 32).getData());
        Verify.verify(Header.MINI_SECTOR_SHIFT_VERSION_3 == dataView.subView(32, 34).getData());
        Verify.verify(Header.MINI_STREAM_CUTOFF_SIZE == dataView.subView(56, 60).getData());
    }
}
