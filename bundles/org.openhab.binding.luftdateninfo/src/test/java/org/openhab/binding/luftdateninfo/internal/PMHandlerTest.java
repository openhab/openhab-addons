package org.openhab.binding.luftdateninfo.internal;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;
import org.openhab.binding.luftdateninfo.internal.mock.PMHandlerExtension;
import org.openhab.binding.luftdateninfo.internal.mock.ThingMock;

public class PMHandlerTest {

    @Test
    public void testValidConfigStatus() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", "12345");
        t.setConfiguration(properties);

        PMHandlerExtension pmHandler = new PMHandlerExtension(t);
        pmHandler.initialize();
        System.out.println("LC status: " + pmHandler.getLifecycleStatus());
        int retryCount = 0; // Test shall fail after max 10 seconds
        while (pmHandler.getLifecycleStatus() != 0 && retryCount < 20) {
            try {
                System.out.println("LC running not reached - wait");
                Thread.sleep(500);
                retryCount++;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        /*
         * Test if config status is 0 = CONFIG_OK for valid configuration. Take real int for comparison instead of
         * BaseHandler constants - in case of change test needs to be adapted
         */
        assertEquals("Handler Configuration status", 0, pmHandler.getConfigStatus());
    }

    @Test
    public void testInvalidConfigStatus() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", "abcdefg");
        t.setConfiguration(properties);

        PMHandlerExtension pmHandler = new PMHandlerExtension(t);
        pmHandler.initialize();
        System.out.println("LC status: " + pmHandler.getLifecycleStatus());
        int retryCount = 0; // Test shall fail after max 10 seconds
        while (pmHandler.getLifecycleStatus() != 0 && retryCount < 20) {
            try {
                System.out.println("LC running not reached - wait");
                Thread.sleep(500);
                retryCount++;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        /*
         * Test if config status is 3 = CONFIG_SENSOR_NUMBER for invalid configuration with non-number sensorid. Take
         * real int for comparison instead of BaseHandler constants - in case of change test needs to be adapted
         */
        assertEquals("Handler Configuration status", 3, pmHandler.getConfigStatus());
    }
}
