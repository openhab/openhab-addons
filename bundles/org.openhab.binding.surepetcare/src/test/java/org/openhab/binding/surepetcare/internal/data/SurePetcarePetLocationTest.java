package org.openhab.binding.surepetcare.internal.data;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.openhab.binding.surepetcare.internal.GsonColonDateTypeAdapter;
import org.openhab.binding.surepetcare.internal.data.SurePetcarePetLocation.PetLocation;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SurePetcarePetLocationTest {

    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Date.class, new GsonColonDateTypeAdapter()).create();

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    @Test
    public void testJsonDeserialize() throws ParseException {
        String testReponse = "{\"pet_id\":70237,\"tag_id\":60126,\"device_id\":376236,\"where\":2,\"since\":\"2019-09-11T13:09:07+00:00\"}";
        SurePetcarePetLocation response = gson.fromJson(testReponse, SurePetcarePetLocation.class);

        assertEquals(new Integer(70237), response.getPetId());
        assertEquals(new Integer(60126), response.getTagId());
        assertEquals(new Integer(376236), response.getDeviceId());
        assertEquals(PetLocation.OUTSIDE.getLocationId(), response.getWhere());
        assertEquals("Outside", response.getLocationName());
        Date sinceDate = simpleDateFormat.parse("2019-09-11T13:09:07+0000");
        assertEquals(sinceDate, response.getSince());
    }

    @Test
    public void testJsonFullSerialize() throws ParseException {

        Date since = simpleDateFormat.parse("2019-09-11T13:09:07+0000");

        SurePetcarePetLocation location = new SurePetcarePetLocation(PetLocation.OUTSIDE, since);

        String json = gson.toJson(location, SurePetcarePetLocation.class);

        assertEquals("{\"where\":2,\"since\":\"2019-09-11T13:09:07+00:00\"}", json);
    }

}
