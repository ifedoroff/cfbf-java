package com.ifedorov.cfbf;

import java.util.List;

public class MiniStream {


    private final List<Sector> miniStreamSectors;
    private final int streamSectorSize;

    public MiniStream(List<Sector> miniStreamSectors, int streamSectorSize) {
        this.miniStreamSectors = miniStreamSectors;
        this.streamSectorSize = streamSectorSize;
    }



}
