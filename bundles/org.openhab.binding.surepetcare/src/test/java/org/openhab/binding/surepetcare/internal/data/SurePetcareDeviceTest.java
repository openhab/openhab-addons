package org.openhab.binding.surepetcare.internal.data;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Date;

import org.junit.Test;
import org.openhab.binding.surepetcare.internal.GsonColonDateTypeAdapter;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SurePetcareDeviceTest {

    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Date.class, new GsonColonDateTypeAdapter()).create();

    // private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    @Test
    public void testJsonDeserializeHub() throws ParseException {
        String testReponse = "{\"id\":296464,\"product_id\":1,\"household_id\":48712,\"name\":\"Home Hub\",\"serial_number\":\"H008-0296432\",\"mac_address\":\"00000491630A0D64\",\"version\":\"NjA=\",\"created_at\":\"2019-04-18T14:45:11+00:00\",\"updated_at\":\"2019-09-30T12:31:52+00:00\",\"control\":{\"led_mode\":4,\"pairing_mode\":0},\"status\":{\"led_mode\":4,\"pairing_mode\":0,\"version\":{\"device\":{\"hardware\":3,\"firmware\":1.772}},\"online\":true}}";
        SurePetcareDevice response = gson.fromJson(testReponse, SurePetcareDevice.class);

        assertEquals(new Integer(296464), response.getId());
        assertEquals(new Integer(1), response.getProductId());
        assertEquals(new Integer(48712), response.getHouseholdId());
        assertEquals("Home Hub", response.getName());
        assertEquals("H008-0296432", response.getSerialNumber());
        assertEquals("00000491630A0D64", response.getMacAddress());
        assertEquals("NjA=", response.getVersion());
        assertEquals(new Integer(4), response.getControl().ledMode);
        assertEquals(new Integer(0), response.getControl().pairingMode);
        assertEquals(new Integer(4), response.getStatus().ledMode);
        assertEquals(new Integer(0), response.getStatus().pairingMode);
        assertEquals("3", response.getStatus().version.device.hardware);
        assertEquals("1.772", response.getStatus().version.device.firmware);
    }

}
