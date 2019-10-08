package com.ifedorov.cfbf;

import com.ifedorov.cfbf.alloc.DIFAT;
import com.ifedorov.cfbf.alloc.FAT;
import com.ifedorov.cfbf.alloc.FATtoDIFATFacade;
import com.ifedorov.cfbf.alloc.MiniFAT;
import com.ifedorov.cfbf.stream.ConditionalStreamRW;
import com.ifedorov.cfbf.stream.MiniStreamRW;
import com.ifedorov.cfbf.stream.RegularStreamRW;

import java.util.stream.Collectors;

public class CompoundFile {

    private final Header header;
    private final DIFAT difat;
    private final Sectors sectors;
    private final FAT fat;
    private final MiniFAT miniFat;
    private final DirectoryEntryChain directoryEntryChain;
    private DataView dataView;

    public CompoundFile(DataView dataView) {
        this.dataView = dataView;
        this.header = new Header(dataView.subView(0, Header.HEADER_LENGTH));
        this.sectors = new Sectors(dataView, header);
        FATtoDIFATFacade faTtoDIFATFacade = new FATtoDIFATFacade();
        difat = new DIFAT(sectors, header, faTtoDIFATFacade);
        faTtoDIFATFacade.setDifat(difat);
        fat = new FAT(sectors, header, faTtoDIFATFacade);
        faTtoDIFATFacade.setFat(fat);
        miniFat = new MiniFAT(sectors, header, fat);
        ConditionalStreamRW streamReader = new ConditionalStreamRW(
                new RegularStreamRW(fat, sectors),
                new MiniStreamRW(miniFat, fat.buildChain(getMiniStreamFirstSectorLocation()),sectors, header),
                header.getMiniStreamCutoffSize()
        );
        directoryEntryChain = new DirectoryEntryChain(sectors, fat, header, streamReader);
    }

    private int getMiniStreamFirstSectorLocation() {
        return Utils.toInt(sectors.sector(header.getFirstDirectorySectorLocation()).subView(DirectoryEntry.FLAG_POSITION.STARTING_SECTOR_LOCATION, DirectoryEntry.FLAG_POSITION.STARTING_SECTOR_LOCATION + 4).getData());
    }

    public DirectoryEntry getRootStorage() {
        return directoryEntryChain.getEntryById(0);
    }

}
