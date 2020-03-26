package com.ifedorov.cfbf.stream;

import com.google.common.base.Verify;
import org.apache.commons.lang3.ArrayUtils;

public class StreamHolder {

    private final StreamRW regularStreamRW;
    private final StreamRW miniStreamRW;
    private final int sizeThreshold;

    public StreamHolder(StreamRW regularStreamRW, StreamRW miniStreamRW, int sizeThreshold) {
        this.regularStreamRW = regularStreamRW;
        this.miniStreamRW = miniStreamRW;
        this.sizeThreshold = sizeThreshold;
    }

    private StreamRW forSize(int size) {
        if(size >= sizeThreshold) {
            return regularStreamRW;
        } else {
            return miniStreamRW;
        }
    }


    public byte[] getStreamData(int startingLocation, int size) {
        return forSize(size).read(startingLocation, size);
    }

    public int setStreamData(byte[] data) {
        return forSize(data.length).write(data);
    }

    public byte[] read(int startingLocation, int size, int fromIncl, int toExcl) {
        return forSize(size).read(startingLocation, fromIncl, toExcl);
    }

    public void writeAt(int startingLocation, int size, int position, byte[] data) {
        forSize(size).writeAt(startingLocation, position, data);
    }

    public int append(int startingLocation, int size, byte[] data) {
        if(size < sizeThreshold && size + data.length >= sizeThreshold) {
            return forSize(size + data.length).write(ArrayUtils.addAll(forSize(size).read(startingLocation, size), data));
        } else {
            return forSize(size).append(startingLocation, size, data);
        }
    }
}
