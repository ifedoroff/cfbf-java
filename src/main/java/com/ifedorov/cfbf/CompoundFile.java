package com.ifedorov.cfbf;

import com.ifedorov.cfbf.alloc.DIFAT;
import com.ifedorov.cfbf.alloc.FAT;
import com.ifedorov.cfbf.alloc.FATtoDIFATFacade;
import com.ifedorov.cfbf.alloc.MiniFAT;
import com.ifedorov.cfbf.stream.ConditionalStreamRW;
import com.ifedorov.cfbf.stream.MiniStreamRW;
import com.ifedorov.cfbf.stream.RegularStreamRW;
import com.ifedorov.cfbf.stream.StreamRW;

import java.util.stream.Collectors;

public class CompoundFile {

    private final Header header;
    private final DIFAT difat;
    private final Sectors sectors;
    private final FAT fat;
    private final MiniFAT miniFat;
    private final DirectoryEntryChain directoryEntryChain;
    private DataView dataView;

    public CompoundFile() {
       this(empty());
    }

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
        MiniStreamRW miniStreamRW = new MiniStreamRW(miniFat, fat, getMiniStreamFirstSectorLocation(), getMiniStreamLength(), sectors, header);
        StreamRW listenableMiniStream = new StreamRW() {
            @Override
            public byte[] read(int startingSector, int length) {
                return miniStreamRW.read(startingSector, length);
            }

            @Override
            public int write(byte[] data) {
                int firstSectorLocation = miniStreamRW.write(data);
                setMiniStreamFirstSectorLocation(miniStreamRW.getMiniStreamFirstSectorPosition());
                setMiniStreamLength(miniStreamRW.getMiniStreamLength());
                return firstSectorLocation;
            }
        };
        ConditionalStreamRW streamReader = new ConditionalStreamRW(
                new RegularStreamRW(fat, sectors, header),
                listenableMiniStream,
                header.getMiniStreamCutoffSize()
        );

        directoryEntryChain = new DirectoryEntryChain(sectors, fat, header, streamReader);
    }

    private static DataView empty() {
        DataView dataView = DataView.empty();
        Header.empty(dataView.allocate(Header.HEADER_LENGTH));
        return dataView;
    }

    private int getMiniStreamFirstSectorLocation() {
        if(Utils.ENDOFCHAIN_MARK_INT == header.getFirstDirectorySectorLocation()) {
            return Utils.ENDOFCHAIN_MARK_INT;
        } else {
            return Utils.toInt(sectors.sector(header.getFirstDirectorySectorLocation()).subView(DirectoryEntry.FLAG_POSITION.STARTING_SECTOR_LOCATION, DirectoryEntry.FLAG_POSITION.STARTING_SECTOR_LOCATION + 4).getData());
        }
    }

    private int getMiniStreamLength() {
        if(Utils.ENDOFCHAIN_MARK_INT == header.getFirstDirectorySectorLocation()) {
            return Utils.FREESECT_MARK_OR_NOSTREAM_INT;
        } else {
            return Utils.toInt(sectors.sector(header.getFirstDirectorySectorLocation()).subView(DirectoryEntry.FLAG_POSITION.STREAM_SIZE, DirectoryEntry.FLAG_POSITION.STREAM_SIZE + 4).getData());
        }
    }

    private void setMiniStreamFirstSectorLocation(int position) {
        sectors.sector(header.getFirstDirectorySectorLocation())
                .subView(DirectoryEntry.FLAG_POSITION.STARTING_SECTOR_LOCATION, DirectoryEntry.FLAG_POSITION.STARTING_SECTOR_LOCATION + 4)
                .writeAt(0, position >= 0 ? Utils.toBytes(position, 4) : Utils.ENDOFCHAIN_MARK);
    }

    private void setMiniStreamLength(int size) {
        sectors.sector(header.getFirstDirectorySectorLocation())
                .subView(DirectoryEntry.FLAG_POSITION.STREAM_SIZE, DirectoryEntry.FLAG_POSITION.STREAM_SIZE + 4)
                .writeAt(0, Utils.toBytes(size, 4));
    }

    public DirectoryEntry getRootStorage() {
        return directoryEntryChain.getEntryById(0);
    }

}
