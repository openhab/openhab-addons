package org.openhab.binding.kodi.internal.protocol;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class KodiConnectionTest {
    @Test
    public void testInputButtonEventOneParam() {
        // TODO: Check method with string containing one parameter
        // kodiConnection.inputButtonEvent("firstParam");
        assertTrue(true);
    }

    @Test
    public void testInputButtonEventTwoParams() {
        // TODO: Check method with string containing two parameters
        // kodiConnection.inputButtonEvent("firstParam;secondParam");
        assertTrue(true);
    }

    @Test
    public void testInputButtonEventThreeParams() {
        // TODO: Check method with string containing three parameters
        // kodiConnection.inputButtonEvent("firstParam;secondParam;4711");
        assertTrue(true);
    }

    @Test
    public void testInputButtonEventThreeParamsWrongThirdParam() {
        // TODO: Check method with string containing three parameters and a missformed third parameter
        // kodiConnection.inputButtonEvent("firstParam;secondParam;notANumber");
        assertTrue(true);
    }
}
