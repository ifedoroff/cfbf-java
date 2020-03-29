package com.ifedorov.cfbf;

import com.google.common.base.VerifyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.stream.IntStream;

import static com.ifedorov.cfbf.Header.HEADER_LENGTH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(DIFATSectorTest.DIFATSectorDataResolver.class)
class DIFATSectorTest {

    @Test
    void testRegisterFATSector(DIFATSector sector) {
        IntStream.range(0, 127).boxed().forEach((val) -> sector.registerFatSector(val));
        assertThrows(VerifyException.class, () -> sector.registerFatSector(127));
    }

    @Test
    void testGetRegisteredFATSectors() {
        byte[] data = new byte[Header.SECTOR_SHIFT_VERSION_3_INT];
        IntStream.range(0, 128).boxed().forEach((val) -> System.arraycopy(Utils.toBytesLE(val, 4), 0, data, val*4, 4));
        DIFATSector sector = new DIFATSector(Sector.from(DataView.from(data), 0));
        assertEquals(127, sector.getRegisteredFatSectors().size());
    }

    @Test
    void testRegisterDIFATSector(DIFATSector sector) {
        sector.registerNextDifatSector(1);
        Arrays.equals(Utils.toBytesLE(1, 4), sector.subView(508).getData());
    }

    public static class DIFATSectorDataResolver implements ParameterResolver {
        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            return parameterContext.getParameter().getType() == DIFATSector.class;
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            Header header = mock(Header.class);
            when(header.getSectorShift()).thenReturn(HEADER_LENGTH);
            return new Sectors(DataView.empty(), header).allocateDIFAT();
        }
    }
}