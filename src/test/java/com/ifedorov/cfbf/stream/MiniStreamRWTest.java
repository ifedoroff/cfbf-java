package com.ifedorov.cfbf.stream;

import com.google.common.collect.Lists;
import com.ifedorov.cfbf.*;
import com.ifedorov.cfbf.alloc.FAT;
import com.ifedorov.cfbf.alloc.MiniFAT;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static com.ifedorov.cfbf.Header.HEADER_LENGTH;
import static com.ifedorov.cfbf.stream.MiniStreamRW.MINI_STREAM_CHUNK_SIZE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MiniStreamRWTest {

    @Mock MiniFAT miniFAT;
    @Mock FAT fat;
    @Mock Sectors sectors;
    @Mock Header header;

    @BeforeEach
    void init() {
        lenient().when(header.getMiniSectorShift()).thenReturn(64);
        lenient().when(header.getSectorShift()).thenReturn(HEADER_LENGTH);
    }

    @Test
    void testRead() {
        when(miniFAT.buildChain(0)).thenReturn(Lists.newArrayList(0,1,2,3,4,5,6,7,8,9));
        byte[] firstSectorData = new byte[HEADER_LENGTH];
        System.arraycopy(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 0), 0, firstSectorData, 0, 64);
        System.arraycopy(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 1), 0, firstSectorData, 64, 64);
        System.arraycopy(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 2), 0, firstSectorData, 128, 64);
        System.arraycopy(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 3), 0, firstSectorData, 192, 64);
        System.arraycopy(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 4), 0, firstSectorData, 256, 64);
        System.arraycopy(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 5), 0, firstSectorData, 320, 64);
        System.arraycopy(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 6), 0, firstSectorData, 384, 64);
        System.arraycopy(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 7), 0, firstSectorData, 448, 64);
        Sector firstSector = Sector.from(DataView.from(firstSectorData), 0);
        byte[] secondSectorData = new byte[HEADER_LENGTH];
        System.arraycopy(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 8), 0, secondSectorData, 0, 64);
        Sector secondSector = Sector.from(DataView.from(secondSectorData), 1);
        when(sectors.sector(0)).thenReturn(firstSector);
        when(sectors.sector(1)).thenReturn(secondSector);
        when(fat.buildChain(0)).thenReturn(Lists.newArrayList(0, 1));
        MiniStreamRW miniStreamRW = new MiniStreamRW(miniFAT, fat, 0, 8, sectors, header);
        byte[] result = miniStreamRW.read(0, 516);
        assertEquals(516, result.length);
        verify(miniFAT, times(1)).buildChain(0);
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 0), ArrayUtils.subarray(result, 0, 64));
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 1), ArrayUtils.subarray(result, 64, 128));
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 2), ArrayUtils.subarray(result, 128, 192));
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 3), ArrayUtils.subarray(result, 192, 256));
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 4), ArrayUtils.subarray(result, 256, 320));
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 5), ArrayUtils.subarray(result, 320, 384));
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 6), ArrayUtils.subarray(result, 384, 448));
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 7), ArrayUtils.subarray(result, 448, 512));
        assertArrayEquals(Utils.initializedWith(4, 8), ArrayUtils.subarray(result, 512, 516));
    }

    @Test
    void testWriteFirstChain() {
        Sectors sectors = new Sectors(DataView.empty(), header);
        MiniStreamRW miniStreamRW = new MiniStreamRW(miniFAT, fat, -1, 0, sectors, header);
        byte[] data = new byte[520];
        IntStream.range(0, 520).forEach(val -> data[val] = (byte)(val/MINI_STREAM_CHUNK_SIZE));
        miniStreamRW.write(data);
        verify(fat, times(1)).registerSector(0, null);
        verify(fat, times(1)).registerSector(1, 0);
        verify(miniFAT, times(1)).registerSector(0, null);
        verify(miniFAT, times(1)).registerSector(1, 0);
        verify(miniFAT, times(1)).registerSector(2, 1);
        verify(miniFAT, times(1)).registerSector(3, 2);
        verify(miniFAT, times(1)).registerSector(4, 3);
        verify(miniFAT, times(1)).registerSector(5, 4);
        verify(miniFAT, times(1)).registerSector(6, 5);
        verify(miniFAT, times(1)).registerSector(7, 6);
        verify(miniFAT, times(1)).registerSector(8, 7);
        Sector firstSector = sectors.sector(0);
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 0), firstSector.subView(0, 64).getData());
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 1), firstSector.subView(64, 128).getData());
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 2), firstSector.subView(128, 192).getData());
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 3), firstSector.subView(192, 256).getData());
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 4), firstSector.subView(256, 320).getData());
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 5), firstSector.subView(320, 384).getData());
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 6), firstSector.subView(384, 448).getData());
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 7), firstSector.subView(448, 512).getData());
        Sector secondSector = sectors.sector(1);
        assertArrayEquals(Utils.initializedWith(8, 8), secondSector.subView(0,8).getData());
        assertEquals(9 * header.getMiniSectorShift(), miniStreamRW.getMiniStreamLength());
        assertEquals(0, miniStreamRW.getMiniStreamFirstSectorPosition());
    }

    @Test
    void testWriteNotFirstChain() {
        Sectors sectors = new Sectors(DataView.empty(), header);
        sectors.allocate();
        sectors.allocate();
        when(fat.buildChain(0)).thenReturn(Lists.newArrayList(0,1));
        MiniStreamRW miniStreamRW = new MiniStreamRW(miniFAT, fat, 0, 10 * header.getMiniSectorShift(), sectors, header);
        byte[] data = new byte[520];
        IntStream.range(0, 520).forEach(val -> data[val] = (byte)(val/64));
        miniStreamRW.write(data);
        verify(fat, times(1)).registerSector(2, 1);
        verify(miniFAT, times(1)).registerSector(10, null);
        verify(miniFAT, times(1)).registerSector(11, 10);
        verify(miniFAT, times(1)).registerSector(12, 11);
        verify(miniFAT, times(1)).registerSector(13, 12);
        verify(miniFAT, times(1)).registerSector(14, 13);
        verify(miniFAT, times(1)).registerSector(15, 14);
        verify(miniFAT, times(1)).registerSector(16, 15);
        verify(miniFAT, times(1)).registerSector(17, 16);
        verify(miniFAT, times(1)).registerSector(18, 17);
        Sector sector1 = sectors.sector(1);
        //should be filled with FREESECT since it is default value for a newly allocated sector
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, Utils.FREESECT_MARK_OR_NOSTREAM), sector1.subView(0, 64).getData());
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, Utils.FREESECT_MARK_OR_NOSTREAM), sector1.subView(64, 128).getData());
        //here starts data written during test
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 0), sector1.subView(128, 192).getData());
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 1), sector1.subView(192, 256).getData());
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 2), sector1.subView(256, 320).getData());
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 3), sector1.subView(320, 384).getData());
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 4), sector1.subView(384, 448).getData());
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 5), sector1.subView(448, 512).getData());
        Sector sector2 = sectors.sector(2);
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE, 6), sector2.subView(0, 64).getData());
        assertArrayEquals(Utils.initializedWith(MINI_STREAM_CHUNK_SIZE,  7), sector2.subView(64, 128).getData());
        assertArrayEquals(Utils.initializedWith(8, 8), sector2.subView(128, 136).getData());
        //should be filled with FREESECT since it is default value for a newly allocated sector
        assertArrayEquals(Utils.initializedWith(376, Utils.FREESECT_MARK_OR_NOSTREAM), sector2.subView(136, 512).getData());
        assertEquals(19 * header.getMiniSectorShift(), miniStreamRW.getMiniStreamLength());
        assertEquals(0, miniStreamRW.getMiniStreamFirstSectorPosition());
    }
}