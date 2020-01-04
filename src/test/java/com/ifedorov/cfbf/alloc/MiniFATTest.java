package com.ifedorov.cfbf.alloc;

import com.ifedorov.cfbf.DataView;
import com.ifedorov.cfbf.Header;
import com.ifedorov.cfbf.Sectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class MiniFATTest {
    @Mock
    FATtoDIFATFacade faTtoDIFATFacade;
    @Test
    void shouldSetNumberOfMiniFATSectorsOnHeader() {
        DataView rootView = DataView.empty();
        Header header = Header.empty(rootView.allocate(Header.HEADER_LENGTH));
        Sectors sectors = new Sectors(rootView, header);
        FAT fat = new FAT(sectors, header, faTtoDIFATFacade);
        MiniFAT miniFAT = new MiniFAT(sectors, header, fat);
        miniFAT.registerSector(0, null);
        miniFAT.registerSector(1, 0);
        assertEquals(1, header.getNumberOfMiniFatSectors());
        IntStream.range(0, 128).forEach(i -> miniFAT.registerSector(i + 2, i + 1));
        assertEquals(2, header.getNumberOfMiniFatSectors());
        assertTrue(header.getFirstMinifatSectorLocation() >= 0);
    }

}