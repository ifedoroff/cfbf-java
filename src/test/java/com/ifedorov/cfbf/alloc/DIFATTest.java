package com.ifedorov.cfbf.alloc;

import com.google.common.collect.Lists;
import com.ifedorov.cfbf.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.ifedorov.cfbf.Header.DIFAT_ENTRIES_LIMIT_IN_HEADER;
import static com.ifedorov.cfbf.Header.HEADER_LENGTH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DIFATTest {

    @Mock
    Sectors sectors;
    @Mock
    Header header;
    @Mock FATtoDIFATFacade faTtoDIFATFacade;

    @BeforeEach
    void init() {
        lenient().when(header.getSectorShift()).thenReturn(HEADER_LENGTH);
    }

    @Test
    void testCreationWithTwoSectors() {
        when(header.getDifatEntries()).thenReturn(IntStream.range(0, DIFAT_ENTRIES_LIMIT_IN_HEADER).boxed().collect(Collectors.toList()));
        when(header.getFirstDifatSectorLocation()).thenReturn(0);
        Sectors sectors = new Sectors(DataView.empty(), header);
        Sector firstSector = sectors.allocateDIFAT();
        IntStream.range(0, 127).forEach(val -> firstSector.subView(val * 4, (val + 1) * 4).writeAt(0, Utils.toBytes(val, 4)));
        firstSector.subView(508).writeAt(0, Utils.toBytes(1, 4));
        Sector secondSector = sectors.allocateDIFAT();
        secondSector.subView(0, 4).writeAt(0, Utils.toBytes(0, 4));
        secondSector.subView(508).writeAt(0, Utils.ENDOFCHAIN_MARK);
        assertEquals(237, new DIFAT(sectors, header, faTtoDIFATFacade).getFatSectorChain().size());
    }

    @Test
    void testCreationWithNoSectors() {
        when(header.getDifatEntries()).thenReturn(IntStream.range(0, 100).boxed().collect(Collectors.toList()));
        when(header.getFirstDifatSectorLocation()).thenReturn(Utils.toInt(Utils.ENDOFCHAIN_MARK));
        assertEquals(100, new DIFAT(sectors, header, faTtoDIFATFacade).getFatSectorChain().size());
    }

    @Test
    void testRegisterFatSectorInHeaderAtFirstPosition() {
        when(header.canFitMoreDifatEntries()).thenReturn(true);
        when(header.getFirstDifatSectorLocation()).thenReturn(Utils.toInt(Utils.ENDOFCHAIN_MARK));
        new DIFAT(sectors, header, faTtoDIFATFacade).registerFATSector(1);
        verify(header, times(1)).registerFatSector(1);
    }

    @Test
    void testRegisterFatSectorInHeaderAtLastPosition() {
        when(header.canFitMoreDifatEntries()).thenReturn(true);
        when(header.getFirstDifatSectorLocation()).thenReturn(Utils.toInt(Utils.ENDOFCHAIN_MARK));
        new DIFAT(sectors, header, faTtoDIFATFacade).registerFATSector(1);
        verify(header, times(1)).registerFatSector(1);
    }

    @Test
    void testRegisterFatSectorInFirstDIFATSector() {
        lenient().when(header.canFitMoreDifatEntries()).thenReturn(false);
        when(header.getFirstDifatSectorLocation()).thenReturn(Utils.toInt(Utils.ENDOFCHAIN_MARK));
        DIFATSector sector = mock(DIFATSector.class);
        when(sector.getPosition()).thenReturn(1);
        when(sectors.allocateDIFAT()).thenReturn(sector);
        new DIFAT(sectors, header, faTtoDIFATFacade).registerFATSector(0);
        verify(sectors, times(1)).allocateDIFAT();
        verify(faTtoDIFATFacade, times(1)).registerDifatSectorInFAT(1);
        verify(sector, times(1)).registerFatSector(0);
    }

    @Test
    void testRegisterTwoFatSectorsInFirstDIFATSector() {
        when(header.getFirstDifatSectorLocation()).thenReturn(Utils.toInt(Utils.ENDOFCHAIN_MARK));
        when(header.canFitMoreDifatEntries()).thenReturn(false);
        DIFATSector sector = new DIFATSector(Sector.from(DataView.from(new byte[HEADER_LENGTH]), 1, Utils.FREESECT_MARK_OR_NOSTREAM));
        when(sectors.allocateDIFAT()).thenReturn(sector);

        DIFAT difat = new DIFAT(sectors, header, faTtoDIFATFacade);
        difat.registerFATSector(0);
        difat.registerFATSector(1);

        verify(sectors, times(1)).allocateDIFAT();
        verify(faTtoDIFATFacade, times(1)).registerDifatSectorInFAT(1);
        assertEquals(0, Utils.toInt(sector.subView(0, 4).getData()));
        assertEquals(1, Utils.toInt(sector.subView(4, 8).getData()));
        assertArrayEquals(Utils.FREESECT_MARK_OR_NOSTREAM, sector.subView(8, 12).getData());
    }

    @Test
    void testRegisterTwoFatSectorsInFirstAndSecondDifatSectors() {
        when(header.canFitMoreDifatEntries()).thenReturn(false);
        when(header.getFirstDifatSectorLocation()).thenReturn(0);
        Sectors sectors = new Sectors(DataView.empty(), header);
        DIFATSector firstSector = sectors.allocateDIFAT();
        IntStream.range(0, 126).boxed().forEach((val) -> firstSector.subView(val * 4, (val + 1) * 4).writeAt(0, Utils.toBytes(val, 4)));

        DIFAT difat = new DIFAT(sectors, header, faTtoDIFATFacade);
        difat.registerFATSector(126);
        difat.registerFATSector(127);

        verify(faTtoDIFATFacade, times(1)).registerDifatSectorInFAT(1);
        assertEquals(126, Utils.toInt(firstSector.subView(504, 508).getData()));
        assertEquals(1, Utils.toInt(firstSector.subView(508).getData()));
        assertEquals(127, Utils.toInt(sectors.sector(1).subView(0, 4).getData()));
    }

    @Test
    void shouldSetNumberOfDIFATSectorsOnHeader() {
        DataView rootView = DataView.empty();
        Header header = Header.empty(rootView.allocate(Header.HEADER_LENGTH));
        Sectors sectors = new Sectors(rootView, header);
        FAT fat = new FAT(sectors, header, faTtoDIFATFacade);
        DIFAT difat = new DIFAT(sectors, header, faTtoDIFATFacade);
        IntStream.range(0, DIFAT_ENTRIES_LIMIT_IN_HEADER + 2).forEach(i -> difat.registerFATSector(i));
        assertTrue(header.getFirstDifatSectorLocation() >= 0);
        assertEquals(1, header.getNumberOfDifatSectors());
    }
}