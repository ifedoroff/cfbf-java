package com.ifedorov.cfbf.stream;

import com.ifedorov.cfbf.FAT;

public class ConditionalStreamReader implements StreamReader{

    private final RegularStreamReader regularStreamReader;
    private final MiniStreamReader miniStreamReader;
    private final int sizeTheshold;

    public ConditionalStreamReader(RegularStreamReader regularStreamReader, MiniStreamReader miniStreamReader, int sizeTheshold) {
        this.regularStreamReader = regularStreamReader;
        this.miniStreamReader = miniStreamReader;
        this.sizeTheshold = sizeTheshold;
    }


    @Override
    public byte[] read(int startingSector, int length) {
        if(length >= sizeTheshold) {
            return regularStreamReader.read(startingSector, length);
        } else {
            return miniStreamReader.read(startingSector, length);
        }
    }
}
