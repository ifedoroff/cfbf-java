package com.ifedorov.cfbf;

import com.google.common.base.Verify;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

public class DIFATSector implements Sector {

    private Sector delegate;
    private List<Integer> fatSectors = Lists.newArrayList();

    public DIFATSector(Sector delegate) {
        this.delegate = delegate;
        for (int i = 0; i < delegate.getSize() - 4 - 1; i+=4) {
            byte[] fatSectorPosition = delegate.subView(i, i + 4).getData();
            if(Arrays.equals(fatSectorPosition, Utils.FREESECT_MARK_OR_NOSTREAM)) {
                break;
            } else {
                fatSectors.add(Utils.toInt(fatSectorPosition));
            }
        }
    }

    @Override
    public int getPosition() {
        return delegate.getPosition();
    }

    @Override
    public DataView writeAt(int position, byte[] bytes) {
        Verify.verify(!Utils.isEndOfChain(bytes) || position == 508);
        Verify.verify(!Utils.isFreeSectOrNoStream(bytes) || fatSectors.size() <= position / 4);
        if(!Utils.isEndOfChain(bytes) && !Utils.isFreeSectOrNoStream(bytes) && position != 508) {
            Verify.verify(fatSectors.size() == position / 4);
        }
        fatSectors.add(Utils.toInt(bytes));
        return delegate.writeAt(position, bytes);
    }

    public void registerFatSector(int sectorPosition) {
        Verify.verify(fatSectors.size() < 127);
        writeAt(fatSectors.size() * 4, Utils.toBytes(sectorPosition, 4));
    }

    public void registerNextDifatSector(int sectorPosition) {
        writeAt(508, Utils.toBytes(sectorPosition, 4));
    }

    public List<Integer> getRegisteredFatSectors() {
        return fatSectors;
    }

    public boolean hasFreeSpace() {
        return fatSectors.size() < 127;
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    @Override
    public byte[] getData() {
        return delegate.getData();
    }

    @Override
    public DataView subView(int start, int end) {
        return delegate.subView(start, end);
    }

    @Override
    public DataView subView(int start) {
        return delegate.subView(start);
    }

    @Override
    public DataView allocate(int length) {
        return delegate.allocate(length);
    }

    @Override
    public DIFATSector fill(byte[] filler) {
        delegate.fill(filler);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public byte[] readAt(int position, int length) {
        return delegate.readAt(position, length);
    }
}
