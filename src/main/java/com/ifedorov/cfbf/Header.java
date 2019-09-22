package com.ifedorov.cfbf;

import com.google.common.base.Verify;
import com.google.common.collect.Lists;

import javax.rmi.CORBA.Util;
import java.util.Arrays;
import java.util.List;

import static com.ifedorov.cfbf.Header.FLAG_POSITION.*;

public class Header {

    public static final byte[] MAJOR_VERSION_3 = Utils.toBytes(0x0003, 2);
    public static final byte[] BYTE_ORDER_LITTLE_ENDIAN = Utils.toBytes(0xfffe, 2);
    public static final byte[] SECTOR_SHIFT_VERSION_3 = Utils.toBytes(0x0009, 2);
    public static final byte[] MINI_SECTOR_SHIFT_VERSION_3 = Utils.toBytes(0x0006, 2);
    public static final byte[] MINI_STREAM_CUTOFF_SIZE = Utils.toBytes(0x00001000, 4);
    public static final int HEADER_LENGTH = 512;
    private final DataView dataView;

    public interface FLAG_POSITION {
        int MAJOR_VERSION = 26;
        int BYTE_ORDER = 28;
        int SECTOR_SHIFT = 30;
        int MINI_SECTOR_SHIFT = 32;
        int MINI_STREAM_CUTOFF_SIZE_POSITION = 56;
        int FIRST_DIRECTORY_SECTOR = 48;
        int NUMBER_OF_FAT_SECTORS = 44;
        int FIRST_MINIFAT_SECTOR = 60;
        int NUMBER_OF_MINIFAT_SECTORS = 64;
        int FIRST_DIFAT_SECTOR = 68;
        int NUMBER_OF_DIFAT_SECTORS = 72;
        int DIFAT_ENTRIES_FIRST_POSITION = 76;
    }

    public Header(DataView dataView) {
        if(dataView.getSize() != HEADER_LENGTH) {
            throw new IndexOutOfBoundsException();
        }
        Verify.verify(Arrays.equals(Header.MAJOR_VERSION_3, dataView.subView(FLAG_POSITION.MAJOR_VERSION, FLAG_POSITION.MAJOR_VERSION +2).getData()));
        Verify.verify(Arrays.equals(Header.BYTE_ORDER_LITTLE_ENDIAN, dataView.subView(FLAG_POSITION.BYTE_ORDER, FLAG_POSITION.BYTE_ORDER + 2).getData()));
        Verify.verify(Arrays.equals(Header.SECTOR_SHIFT_VERSION_3, dataView.subView(FLAG_POSITION.SECTOR_SHIFT, FLAG_POSITION.SECTOR_SHIFT + 2).getData()));
        Verify.verify(Arrays.equals(Header.MINI_SECTOR_SHIFT_VERSION_3, dataView.subView(FLAG_POSITION.MINI_SECTOR_SHIFT, FLAG_POSITION.MINI_SECTOR_SHIFT + 2).getData()));
        Verify.verify(Arrays.equals(new byte[6], dataView.subView(34, 40).getData()));
        Verify.verify(Arrays.equals(new byte[4], dataView.subView(40, 44).getData()));
        Verify.verify(Arrays.equals(Header.MINI_STREAM_CUTOFF_SIZE, dataView.subView(FLAG_POSITION.MINI_STREAM_CUTOFF_SIZE_POSITION, FLAG_POSITION.MINI_STREAM_CUTOFF_SIZE_POSITION + 4).getData()));
        this.dataView = dataView;
    }

    public int getFirstDirectorySectorLocation() {
        return Utils.toInt(dataView.subView(FIRST_DIRECTORY_SECTOR, FIRST_DIRECTORY_SECTOR + 4).getData());
    }

    public int getNumberOfFatSectors() {
        return Utils.toInt(dataView.subView(FLAG_POSITION.NUMBER_OF_FAT_SECTORS, FLAG_POSITION.NUMBER_OF_FAT_SECTORS + 4).getData());
    }

    public int getFirstMinifatSectorLocation() {
        return Utils.toInt(dataView.subView(FLAG_POSITION.FIRST_MINIFAT_SECTOR, FLAG_POSITION.FIRST_MINIFAT_SECTOR + 4).getData());
    }

    public int getFirstDifatSectorLocation() {
        return Utils.toInt(dataView.subView(FIRST_DIFAT_SECTOR, FIRST_DIFAT_SECTOR + 4).getData());
    }

    public int getNumberOfMiniFatSectors() {
        return Utils.toInt(dataView.subView(NUMBER_OF_MINIFAT_SECTORS, NUMBER_OF_MINIFAT_SECTORS + 4).getData());
    }

    public int getNumberOfDifatSectors() {
        return Utils.toInt(dataView.subView(NUMBER_OF_DIFAT_SECTORS, NUMBER_OF_DIFAT_SECTORS + 4).getData());
    }

    public List<Integer> getDifatEntries() {
        List<Integer> difatEntries = Lists.newArrayList();
        for (int i = DIFAT_ENTRIES_FIRST_POSITION; i < HEADER_LENGTH; i+=4) {
            byte[] entry = dataView.subView(i, i+4).getData();
            if(Utils.isFreeSectOrNoStream(entry)) {
                break;
            }
            difatEntries.add(Utils.toInt(entry));
        }
        return difatEntries;
    }

    public void setNumberOfFatSectors(int i) {
        dataView.subView(NUMBER_OF_FAT_SECTORS, NUMBER_OF_FAT_SECTORS + 4).writeAt(0, Utils.toBytes(i, 4));
    }

    public void setFirstDirectorySectorLocation(int i) {
        dataView.subView(FIRST_DIRECTORY_SECTOR, FIRST_DIRECTORY_SECTOR + 4).writeAt(0, Utils.toBytes(i, 4));
    }

    public void setFirstMinifatSectorLocation(int i) {
        dataView.subView(FIRST_MINIFAT_SECTOR, FIRST_MINIFAT_SECTOR + 4).writeAt(0, Utils.toBytes(i, 4));
    }

    public void setNumberOfMiniFatSectors(int i) {
        dataView.subView(NUMBER_OF_MINIFAT_SECTORS, NUMBER_OF_MINIFAT_SECTORS + 4).writeAt(0, Utils.toBytes(i, 4));
    }

    public void setFirstDifatSectorLocation(int i) {
        dataView.subView(FIRST_DIFAT_SECTOR, FIRST_DIFAT_SECTOR + 4).writeAt(0, Utils.toBytes(i, 4));
    }

    public void setNumberOfDifatSectors(int i) {
        dataView.subView(NUMBER_OF_DIFAT_SECTORS, NUMBER_OF_DIFAT_SECTORS + 4).writeAt(0, Utils.toBytes(i, 4));
    }

    public int getSectorShift() {
        return (int)Math.pow(2, Utils.toInt(dataView.subView(SECTOR_SHIFT, SECTOR_SHIFT + 2).getData()));
    }

    public int getMiniSectorShift() {
        return (int)Math.pow(2, Utils.toInt(dataView.subView(MINI_SECTOR_SHIFT, MINI_SECTOR_SHIFT + 2).getData()));
    }

    public int getMiniStreamCutoffSize() {
        return Utils.toInt(dataView.subView(MINI_STREAM_CUTOFF_SIZE_POSITION, MINI_STREAM_CUTOFF_SIZE_POSITION + 4).getData());
    }
}
