package com.ifedorov.cfbf.stream;

public class ConditionalStreamRW implements StreamRW {

    private final RegularStreamRW regularStreamRW;
    private final MiniStreamRW miniStreamRW;
    private final int sizeThreshold;

    public ConditionalStreamRW(RegularStreamRW regularStreamRW, MiniStreamRW miniStreamRW, int sizeThreshold) {
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
        return 0;
    }
}
