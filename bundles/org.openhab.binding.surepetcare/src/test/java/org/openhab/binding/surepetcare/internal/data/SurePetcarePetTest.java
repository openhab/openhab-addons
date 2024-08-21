/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.surepetcare.internal.data;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.surepetcare.internal.SurePetcareConstants;
import org.openhab.binding.surepetcare.internal.dto.SurePetcarePet;

/**
 * The {@link SurePetcarePetTest} class implements unit test case for {@link SurePetcarePet}
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
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

    private static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter ZONED_DATETIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Test
    public void testJsonDeserialize1() throws ParseException {
        String testReponse = "{\"id\":34675,\"name\":\"Cat\",\"gender\":0,\"date_of_birth\":\"2017-08-01T00:00:00+00:00\",\"weight\":\"3.5\",\"comments\":\"Test Comment\",\"household_id\":87435,\"breed_id\":382,\"photo_id\":23412,\"species_id\":1,\"tag_id\":60456,\"version\":\"Mw==\",\"created_at\":\"2019-09-02T09:27:17+00:00\",\"updated_at\":\"2019-10-03T12:17:48+00:00\",\"conditions\":[{\"id\":18,\"version\":\"MA==\",\"created_at\":\"2019-10-03T12:17:48+00:00\",\"updated_at\":\"2019-10-03T12:17:48+00:00\"},{\"id\":17,\"version\":\"MA==\",\"created_at\":\"2019-10-03T12:17:48+00:00\",\"updated_at\":\"2019-10-03T12:17:48+00:00\"}],\"photo\":{\"id\":79293,\"location\":\"https:\\/\\/surehub.s3.amazonaws.com\\/user-photos\\/thm\\/23412\\/z70LUtqaHVhlsdfuyHKJH5HDysg5AR6GvQwdAZptCgeZU.jpg\",\"uploading_user_id\":52815,\"version\":\"MA==\",\"created_at\":\"2019-09-02T09:31:07+00:00\",\"updated_at\":\"2019-09-02T09:31:07+00:00\"},\"position\":{\"tag_id\":60456,\"device_id\":318986,\"where\":1,\"since\":\"2019-10-03T10:23:37+00:00\"},\"status\":{\"activity\":{\"tag_id\":60456,\"device_id\":318986,\"where\":1,\"since\":\"2019-10-03T10:23:37+00:00\"}}}";
        SurePetcarePet response = SurePetcareConstants.GSON.fromJson(testReponse, SurePetcarePet.class);

        if (response != null) {
            assertEquals(Long.valueOf(34675), response.id);
            assertEquals("Cat", response.name);
            assertEquals(Integer.valueOf(0), response.genderId);
            assertEquals(LocalDate.parse("2017-08-01", LOCAL_DATE_FORMATTER), response.dateOfBirth);
            assertEquals(BigDecimal.valueOf(3.5), response.weight);
            assertEquals("Test Comment", response.comments);
            assertEquals(Long.valueOf(87435), response.householdId);
            assertEquals(Long.valueOf(23412), response.photoId);
            assertEquals(SurePetcarePet.PetSpecies.CAT.id, response.speciesId);
            assertEquals(Integer.valueOf(382), response.breedId);
            assertEquals(Integer.valueOf(1), response.status.activity.where);
            assertEquals(ZonedDateTime.parse("2019-10-03T10:23:37+00:00", ZONED_DATETIME_FORMATTER),
                    response.status.activity.since);
        } else {
            fail("GSON returned null");
        }
    }

    @Test
    public void testJsonDeserialize2() throws ParseException {
        String testReponse = "{\"id\":30622,\"name\":\"Cat\",\"gender\":1,\"date_of_birth\":\"2016-04-01T00:00:00+00:00\",\"weight\":\"6\",\"comments\":\"\",\"household_id\":21005,\"breed_id\":382,\"food_type_id\":1,\"photo_id\":77957,\"species_id\":1,\"tag_id\":24725,\"version\":\"OA==\",\"created_at\":\"2018-12-22T08:59:13+00:00\",\"updated_at\":\"2019-08-26T18:17:38+00:00\",\"photo\":{\"id\":77957,\"location\":\"https://surehub.s3.amazonaws.com/user-photos/thm/22360/1jhp4OtwmNvWXrsT4pWLJhoYOt7Ti9UVm5SjsFoC9Y.jpg          \",\"uploading_user_id\":22360,\"version\":\"MA==\",\"created_at\":\"2019-08-26T18:17:38+00:00\",\"updated_at\":\"2019-08-26T18:17:38+00:00\"},\"position\":{\"tag_id\":24725,\"device_id\":243573,\"where\":2,\"since\":\"2020-05-01T06:01:53+00:00\"},\"status\":{\"activity\":{\"tag_id\":24725,\"device_id\":243573,\"where\":2,\"since\":\"2020-05-01T06:01:53+00:00\"}}}";
        SurePetcarePet response = SurePetcareConstants.GSON.fromJson(testReponse, SurePetcarePet.class);

        if (response != null) {
            assertEquals(Long.valueOf(30622), response.id);
            assertEquals("Cat", response.name);
            assertEquals(Integer.valueOf(1), response.genderId);
            assertEquals(LocalDate.parse("2016-04-01", LOCAL_DATE_FORMATTER), response.dateOfBirth);
            assertEquals(BigDecimal.valueOf(6), response.weight);
            assertEquals("", response.comments);
            assertEquals(Long.valueOf(21005), response.householdId);
            assertEquals(Long.valueOf(77957), response.photoId);
            assertEquals(SurePetcarePet.PetSpecies.CAT.id, response.speciesId);
            assertEquals(Integer.valueOf(382), response.breedId);
            assertEquals(Integer.valueOf(2), response.status.activity.where);
            assertEquals(ZonedDateTime.parse("2020-05-01T06:01:53+00:00", ZONED_DATETIME_FORMATTER),
                    response.status.activity.since);
        } else {
            fail("GSON returned null");
        }
    }
}
