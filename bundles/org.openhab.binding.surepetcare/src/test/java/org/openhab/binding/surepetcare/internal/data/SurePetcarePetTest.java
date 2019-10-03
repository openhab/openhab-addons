package org.openhab.binding.surepetcare.internal.data;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.openhab.binding.surepetcare.internal.GsonColonDateTypeAdapter;
import org.openhab.binding.surepetcare.internal.data.SurePetcarePetLocation.PetLocation;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SurePetcarePetTest {

    // {
    // "id":34675,
    // "name":"Cat",
    // "gender":0,
    // "date_of_birth":"2017-08-01T00:00:00+00:00",
    // "weight":"3.5",
    // "comments":"Test Comment",
    // "household_id":87435,
    // "breed_id":382,
    // "photo_id":23412,
    // "species_id":1,
    // "tag_id":60456,
    // "version":"Mw==",
    // "created_at":"2019-09-02T09:27:17+00:00",
    // "updated_at":"2019-10-03T12:17:48+00:00",
    // "conditions":[
    // {
    // "id":18,
    // "version":"MA==",
    // "created_at":"2019-10-03T12:17:48+00:00",
    // "updated_at":"2019-10-03T12:17:48+00:00"
    // },
    // {
    // "id":17,
    // "version":"MA==",
    // "created_at":"2019-10-03T12:17:48+00:00",
    // "updated_at":"2019-10-03T12:17:48+00:00"
    // }
    // ],
    // "photo":{
    // "id":79293,
    // "location":"https:\/\/surehub.s3.amazonaws.com\/user-photos\/thm\/23412\/z70LUtqaHVhlsdfuyHKJH5HDysg5AR6GvQwdAZptCgeZU.jpg",
    // "uploading_user_id":52815,
    // "version":"MA==",
    // "created_at":"2019-09-02T09:31:07+00:00",
    // "updated_at":"2019-09-02T09:31:07+00:00"
    // },
    // "position":{
    // "tag_id":60456,
    // "device_id":318986,
    // "where":1,
    // "since":"2019-10-03T10:23:37+00:00"
    // },
    // "status":{
    // "activity":{
    // "tag_id":60456,
    // "device_id":318986,
    // "where":1,
    // "since":"2019-10-03T10:23:37+00:00"
    // }
    // }
    // }

    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Date.class, new GsonColonDateTypeAdapter()).create();

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    @Test
    public void testJsonDeserialize() throws ParseException {
        String testReponse = "{\"id\":34675,\"name\":\"Cat\",\"gender\":0,\"date_of_birth\":\"2017-08-01T00:00:00+00:00\",\"weight\":\"3.5\",\"comments\":\"Test Comment\",\"household_id\":87435,\"breed_id\":382,\"photo_id\":23412,\"species_id\":1,\"tag_id\":60456,\"version\":\"Mw==\",\"created_at\":\"2019-09-02T09:27:17+00:00\",\"updated_at\":\"2019-10-03T12:17:48+00:00\",\"conditions\":[{\"id\":18,\"version\":\"MA==\",\"created_at\":\"2019-10-03T12:17:48+00:00\",\"updated_at\":\"2019-10-03T12:17:48+00:00\"},{\"id\":17,\"version\":\"MA==\",\"created_at\":\"2019-10-03T12:17:48+00:00\",\"updated_at\":\"2019-10-03T12:17:48+00:00\"}],\"photo\":{\"id\":79293,\"location\":\"https:\\/\\/surehub.s3.amazonaws.com\\/user-photos\\/thm\\/23412\\/z70LUtqaHVhlsdfuyHKJH5HDysg5AR6GvQwdAZptCgeZU.jpg\",\"uploading_user_id\":52815,\"version\":\"MA==\",\"created_at\":\"2019-09-02T09:31:07+00:00\",\"updated_at\":\"2019-09-02T09:31:07+00:00\"},\"position\":{\"tag_id\":60456,\"device_id\":318986,\"where\":1,\"since\":\"2019-10-03T10:23:37+00:00\"},\"status\":{\"activity\":{\"tag_id\":60456,\"device_id\":318986,\"where\":1,\"since\":\"2019-10-03T10:23:37+00:00\"}}}";
        SurePetcarePet response = gson.fromJson(testReponse, SurePetcarePet.class);

        assertEquals(new Integer(34675), response.getId());
        assertEquals("Cat", response.getName());
        assertEquals(new Integer(0), response.getGenderId());

        Date dobDate = simpleDateFormat.parse("2017-08-01T00:00:00+0000");
        assertEquals(dobDate, response.getDateOfBirth());

        assertEquals(new BigDecimal("3.5"), response.getWeight());

        assertEquals("Test Comment", response.getComments());
        assertEquals(new Integer(87435), response.getHouseholdId());
        assertEquals(new Integer(23412), response.getPhotoId());
        assertEquals(SurePetcarePet.PetSpecies.CAT.getId(), response.getSpeciesId());
        assertEquals(new Integer(382), response.getBreedId());

        assertEquals(PetLocation.INSIDE.getId(), response.getLocation().getWhere());
        Date sinceDate = simpleDateFormat.parse("2019-10-03T10:23:37+0000");
        assertEquals(sinceDate, response.getLocation().getSince());
    }

}
