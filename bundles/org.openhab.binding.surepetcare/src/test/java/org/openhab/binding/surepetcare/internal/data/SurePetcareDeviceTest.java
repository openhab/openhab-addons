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
        String testResponse = "{\"id\":296464,\"product_id\":1,\"household_id\":48712,\"name\":\"Home Hub\",\"serial_number\":\"H008-0296432\",\"mac_address\":\"00000491630A0D64\",\"version\":\"NjA=\",\"created_at\":\"2019-04-18T14:45:11+00:00\",\"updated_at\":\"2019-09-30T12:31:52+00:00\",\"control\":{\"led_mode\":4,\"pairing_mode\":0},\"status\":{\"led_mode\":4,\"pairing_mode\":0,\"version\":{\"device\":{\"hardware\":3,\"firmware\":1.772}},\"online\":true}}";
        SurePetcareDevice response = gson.fromJson(testResponse, SurePetcareDevice.class);

        assertEquals(new Integer(296464), response.getId());
        assertEquals(new Integer(1), response.getProductId());
        assertEquals(new Integer(48712), response.getHouseholdId());
        assertEquals("Home Hub", response.getName());
        assertEquals("H008-0296432", response.getSerialNumber());
        assertEquals("00000491630A0D64", response.getMacAddress());
        assertEquals("NjA=", response.getVersion());
        assertEquals(new Integer(4), response.getControl().ledModeId);
        assertEquals(new Integer(0), response.getControl().pairingModeId);
        assertEquals(new Integer(4), response.getStatus().ledModeId);
        assertEquals(new Integer(0), response.getStatus().pairingModeId);
        assertEquals("3", response.getStatus().version.device.hardware);
        assertEquals("1.772", response.getStatus().version.device.firmware);
    }

    @Test
    public void testJsonDeserializeCatFlap() throws ParseException {
        String testResponse = "{\"id\":318966,\"parent_device_id\":296464,\"product_id\":6,\"household_id\":48712,\"name\":\"Back Door Cat Flap\",\"serial_number\":\"N005-0089709\",\"mac_address\":\"6D5E01CFF9D5B370\",\"index\":0,\"version\":\"MTE5\",\"created_at\":\"2019-05-13T14:09:18+00:00\",\"updated_at\":\"2019-10-01T07:37:20+00:00\",\"pairing_at\":\"2019-09-02T08:24:13+00:00\",\"control\":{\"curfew\":[{\"enabled\":true,\"lock_time\":\"19:30\",\"unlock_time\":\"07:00\"}],\"locking\":0,\"fast_polling\":false},\"parent\":{\"id\":296464,\"product_id\":1,\"household_id\":48712,\"name\":\"Home Hub\",\"serial_number\":\"H008-0296464\",\"mac_address\":\"00000491620A0F60\",\"version\":\"NjE=\",\"created_at\":\"2019-04-18T14:45:11+00:00\",\"updated_at\":\"2019-10-01T07:37:20+00:00\"},\"status\":{\"locking\":{\"mode\":0},\"version\":{\"device\":{\"hardware\":9,\"firmware\":335}},\"battery\":5.771,\"learn_mode\":null,\"online\":true,\"signal\":{\"device_rssi\":-87.25,\"hub_rssi\":-83.5}},\"tags\":[{\"id\":60456,\"index\":0,\"profile\":2,\"version\":\"MA==\",\"created_at\":\"2019-09-02T09:27:17+00:00\",\"updated_at\":\"2019-09-02T09:27:23+00:00\"}]}";
        SurePetcareDevice response = gson.fromJson(testResponse, SurePetcareDevice.class);

        assertEquals(new Integer(318966), response.getId());
        assertEquals(new Integer(6), response.getProductId());
        assertEquals(new Integer(48712), response.getHouseholdId());
        assertEquals("Back Door Cat Flap", response.getName());
        assertEquals("N005-0089709", response.getSerialNumber());
        assertEquals("6D5E01CFF9D5B370", response.getMacAddress());
        assertEquals("MTE5", response.getVersion());
        assertEquals(new Integer(0), response.getStatus().locking.modeId);
    }

}
