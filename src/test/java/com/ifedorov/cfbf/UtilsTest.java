package com.ifedorov.cfbf;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void testUUIDConversion() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, Utils.uuidFromByteLE(Utils.uuidToBytesLE(uuid)));
    }

}