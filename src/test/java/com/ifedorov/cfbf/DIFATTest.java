package com.ifedorov.cfbf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DIFATTest {

    @Mock CompoundFile compoundFile;
    @Mock Header header;

    @BeforeEach
    void init() {
        lenient().when(header.getSectorShift()).thenReturn(512);
    }

    @Test
    void testCreationWithTwoSectors() {
        when(header.getDifatEntries()).thenReturn(IntStream.range(0, 109).boxed().collect(Collectors.toList()));
        when(header.getFirstDifatSectorLocation()).thenReturn(0);
        Sector firstSector = Sector.from(DataView.from(new byte[512]), 0);
        firstSector.subView(508).writeAt(0, Utils.toBytes(1, 4));
        Sector secondSector = Sector.from(DataView.from(new byte[512]), 1);
        secondSector.subView(0, 4).writeAt(0, Utils.toBytes(0, 4));
        secondSector.subView(4, 8).writeAt(0, Utils.ENDOFCHAIN_MARK);
        secondSector.subView(508).writeAt(0, Utils.ENDOFCHAIN_MARK);
        when(compoundFile.sector(0)).thenReturn(firstSector);
        when(compoundFile.sector(1)).thenReturn(secondSector);
        assertEquals(237, new DIFAT(compoundFile, header).getFatSectorChain().size());
    }

    @Test
    void testCreationWithNoSectors() {
        when(header.getDifatEntries()).thenReturn(IntStream.range(0, 100).boxed().collect(Collectors.toList()));
        when(header.getFirstDifatSectorLocation()).thenReturn(Utils.toInt(Utils.ENDOFCHAIN_MARK));
        assertEquals(100, new DIFAT(compoundFile, header).getFatSectorChain().size());
    }
}