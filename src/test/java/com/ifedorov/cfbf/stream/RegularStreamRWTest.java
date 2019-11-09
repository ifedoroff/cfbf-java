package com.ifedorov.cfbf.stream;

import com.google.common.collect.Lists;
import com.ifedorov.cfbf.*;
import com.ifedorov.cfbf.alloc.FAT;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.ifedorov.cfbf.Header.HEADER_LENGTH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegularStreamRWTest {

    @Mock FAT fat;
    @Mock Sectors sectors;
    @Mock Header header;

    @BeforeEach
    void init() {
        lenient().when(header.getSectorShift()).thenReturn(HEADER_LENGTH);
    }

    @Test
    void testRead() {

        when(fat.buildChain(1)).thenReturn(Lists.newArrayList(1,2,3));
        when(sectors.sector(1))
                .thenReturn(Sector.from(DataView.from(Utils.initializedWith(HEADER_LENGTH, 1)), 1));
        when(sectors.sector(2))
                .thenReturn(Sector.from(DataView.from(Utils.initializedWith(HEADER_LENGTH, 2)), 2));
        when(sectors.sector(3))
                .thenReturn(Sector.from(DataView.from(Utils.initializedWith(HEADER_LENGTH, 3)), 3));
        RegularStreamRW regularStreamRW = new RegularStreamRW(fat, sectors, header);
        byte[] result = regularStreamRW.read(1, 1300);
        assertEquals(1300, result.length);
        assertArrayEquals(Utils.initializedWith(HEADER_LENGTH, 1), ArrayUtils.subarray(result, 0, 512));
        assertArrayEquals(Utils.initializedWith(HEADER_LENGTH, 2), ArrayUtils.subarray(result, 512, 1024));
        assertArrayEquals(Utils.initializedWith(276, 3), ArrayUtils.subarray(result, 1024, 1300));
        verify(fat, times(1)).buildChain(1);
        verify(sectors, times(1)).sector(1);
        verify(sectors, times(1)).sector(2);
        verify(sectors, times(1)).sector(3);
    }

    @Test
    void testWrite() {
        Sector first = mock(Sector.class);
        when(first.getPosition()).thenReturn(0);
        Sector second = mock(Sector.class);
        when(second.getPosition()).thenReturn(1);
        Sector third = mock(Sector.class);
        when(third.getPosition()).thenReturn(2);
        Sector fourth = mock(Sector.class);
        when(fourth.getPosition()).thenReturn(3);
        when(sectors.allocate())
                .thenReturn(first).thenReturn(second).thenReturn(third).thenReturn(fourth);
        RegularStreamRW regularStreamRW = new RegularStreamRW(fat, sectors, header);
        byte[] data = new byte[2000];
        regularStreamRW.write(data);

        verify(first, times(1)).writeAt(eq(0), any(byte[].class));
        verify(second, times(1)).writeAt(eq(0), any(byte[].class));
        verify(third, times(1)).writeAt(eq(0), any(byte[].class));
        ArgumentCaptor<byte[]> valueCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(fourth, times(1)).writeAt(eq(0), valueCaptor.capture());
        assertEquals(464, valueCaptor.getValue().length);
        verify(sectors, times(4)).allocate();
        verify(fat, times(1)).registerSector(0, null);
        verify(fat, times(1)).registerSector(1, 0);
        verify(fat, times(1)).registerSector(2, 1);
        verify(fat, times(1)).registerSector(3, 2);
    }
}