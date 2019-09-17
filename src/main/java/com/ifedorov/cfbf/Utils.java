package com.ifedorov.cfbf;

import java.util.Arrays;

public class Utils {
    public static final byte[] DISECT_MARK = Utils.toBytes(0xfffffffc,4 );
    public static final byte[] FATSECT_MARK = Utils.toBytes(0xfffffffd, 4);
    public static final byte[] ENDOFCHAIN_MARK = Utils.toBytes(0xfffffffe, 4);
    public static final byte[] FREESECT_MARK_OR_NOSTREAM = Utils.toBytes(0xffffffff, 4);
    public static final byte[] MAX_POSSIBLE_POSITION = Utils.toBytes(0xfffffffa, 4);

    public static byte[] toBytes(long l, int length) {
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = (byte)(l & 0xFF);
            l >>= 8;
        }
        return result;
    }

    public static long toLong(byte[] b) {
        long result = 0;
        for (int i = b.length - 1; i >= 0; i--) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    public static int toInt(byte[] bytes) {
        if(bytes.length == 2) {
            return (((bytes[1]<< 8 & 0xFF)) | (bytes[0] & 0xFF));
        } else if(bytes.length == 4) {
            return ((bytes[0] & 0xFF) << 0) |
                    ((bytes[1] & 0xFF) << 8) |
                    ((bytes[2] & 0xFF) << 16) |
                    ((bytes[3] & 0xFF) << 24);
        } else {
            throw new IllegalArgumentException("Cannot convert bytes to int: " + Arrays.toString(bytes));
        }
    }
}
