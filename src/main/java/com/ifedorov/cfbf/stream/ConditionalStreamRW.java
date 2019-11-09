package com.ifedorov.cfbf.stream;

public class ConditionalStreamRW implements StreamRW {

    private final StreamRW regularStreamRW;
    private final StreamRW miniStreamRW;
    private final int sizeThreshold;

    public ConditionalStreamRW(StreamRW regularStreamRW, StreamRW miniStreamRW, int sizeThreshold) {
        this.regularStreamRW = regularStreamRW;
        this.miniStreamRW = miniStreamRW;
        this.sizeThreshold = sizeThreshold;
    }


    @Override
    public byte[] read(int startingSector, int length) {
        if(length >= sizeThreshold) {
            return regularStreamRW.read(startingSector, length);
        } else {
            return miniStreamRW.read(startingSector, length);
        }
    }

    @Override
    public int write(byte[] data) {
        if(data.length >= sizeThreshold) {
            return regularStreamRW.write(data);
        } else {
            return miniStreamRW.write(data);
        }
    }
}
