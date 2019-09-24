package com.ifedorov.cfbf.stream;

import com.google.common.collect.Lists;
import com.ifedorov.cfbf.*;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MiniStreamRWTest {

    @Mock MiniFAT miniFAT;
    @Mock CompoundFile compoundFile;

    @Test
    void testRead() {
        when(miniFAT.buildChain(0)).thenReturn(Lists.newArrayList(0,1,2,3,4,5,6,7,8,9));
        byte[] firstSectorData = new byte[512];
        System.arraycopy(Utils.initializedWith(64, 0), 0, firstSectorData, 0, 64);
        System.arraycopy(Utils.initializedWith(64, 1), 0, firstSectorData, 64, 64);
        System.arraycopy(Utils.initializedWith(64, 2), 0, firstSectorData, 128, 64);
        System.arraycopy(Utils.initializedWith(64, 3), 0, firstSectorData, 192, 64);
        System.arraycopy(Utils.initializedWith(64, 4), 0, firstSectorData, 256, 64);
        System.arraycopy(Utils.initializedWith(64, 5), 0, firstSectorData, 320, 64);
        System.arraycopy(Utils.initializedWith(64, 6), 0, firstSectorData, 384, 64);
        System.arraycopy(Utils.initializedWith(64, 7), 0, firstSectorData, 448, 64);
        Sector firstSector = Sector.from(DataView.from(firstSectorData), 0);
        byte[] secondSectorData = new byte[512];
        System.arraycopy(Utils.initializedWith(64, 8), 0, secondSectorData, 0, 64);
        Sector secondSector = Sector.from(DataView.from(secondSectorData), 1);
        when(compoundFile.sector(0)).thenReturn(firstSector);
        when(compoundFile.sector(1)).thenReturn(secondSector);
        MiniStreamRW miniStreamRW = new MiniStreamRW(miniFAT, Lists.newArrayList(0, 1), compoundFile, 64, 512);
        byte[] result = miniStreamRW.read(0, 516);
        assertEquals(516, result.length);
        verify(miniFAT, times(1)).buildChain(0);
        assertArrayEquals(Utils.initializedWith(64, 0), ArrayUtils.subarray(result, 0, 64));
        assertArrayEquals(Utils.initializedWith(64, 1), ArrayUtils.subarray(result, 64, 128));
        assertArrayEquals(Utils.initializedWith(64, 2), ArrayUtils.subarray(result, 128, 192));
        assertArrayEquals(Utils.initializedWith(64, 3), ArrayUtils.subarray(result, 192, 256));
        assertArrayEquals(Utils.initializedWith(64, 4), ArrayUtils.subarray(result, 256, 320));
        assertArrayEquals(Utils.initializedWith(64, 5), ArrayUtils.subarray(result, 320, 384));
        assertArrayEquals(Utils.initializedWith(64, 6), ArrayUtils.subarray(result, 384, 448));
        assertArrayEquals(Utils.initializedWith(64, 7), ArrayUtils.subarray(result, 448, 512));
        assertArrayEquals(Utils.initializedWith(4, 8), ArrayUtils.subarray(result, 512, 516));
    }
}