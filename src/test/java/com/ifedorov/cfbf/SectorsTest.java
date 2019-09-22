package com.ifedorov.cfbf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.ifedorov.cfbf.HeaderTest.dummyHeader;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SectorsTest.SectorsDataResolver.class)
class SectorsTest {

    @Test
    void testNumberOfSectors(@SectorsDataResolver.ByNumberOfSectors(numberOfSectors = 5) byte[] data) {
        Sectors sectors = new Sectors(DataView.from(data), 512);
        assertNotNull(sectors.sector(0));
        assertNotNull(sectors.sector(1));
        assertNotNull(sectors.sector(2));
        assertNotNull(sectors.sector(3));
        assertThrows(IndexOutOfBoundsException.class, () -> sectors.sector(4));
    }

    public static class SectorsDataResolver implements ParameterResolver {
        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            return parameterContext.getParameter().getAnnotationsByType(ByNumberOfSectors.class).length > 0;
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            ByNumberOfSectors[] byNumberOfSectors = parameterContext.getParameter().getAnnotationsByType(ByNumberOfSectors.class);
            return createData(byNumberOfSectors[0].numberOfSectors(), byNumberOfSectors[0].sectorSize());
        }

        private byte[] createData(int sectors, int sectorSize) {
            byte[] data = new byte[sectors * sectorSize];
            System.arraycopy(dummyHeader(), 0, data, 0, Header.HEADER_LENGTH);
            return data;
        }

        @Target(ElementType.PARAMETER)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface ByNumberOfSectors {
            int numberOfSectors() default 1;
            int sectorSize() default 512;
        }
    }
}