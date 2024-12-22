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
package org.openhab.binding.metofficedatahub.internal.dto.responses;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.metofficedatahub.internal.MetOfficeDataHubBindingConstants;

/**
 * The {@link SiteApiFeatureCollectionTest} class implements unit test case for {@link SiteApiFeatureCollection}
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class SiteApiFeatureCollectionTest {

    private @Nullable String siteApiDailyResponse = null;
    private @Nullable String siteApiHourlyResponse = null;

    public @Nullable String getSiteDailyApiResponse() {
        try {
            if (siteApiDailyResponse == null) {
                java.net.URL url = SiteApiFeatureCollectionTest.class.getResource("2022-09-siteDailyResponse.json");
                if (url == null) {
                    return null;
                }
                java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
                siteApiDailyResponse = new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
            }
        } catch (Exception e) {
            return null;
        }
       return siteApiDailyResponse;
    }

    public  @Nullable String getSiteHourlyApiResponse() {
        try {
            if (siteApiHourlyResponse == null) {
                java.net.URL url = SiteApiFeatureCollectionTest.class.getResource("2022-09-siteHourlyResponse.json");
                if (url == null) {
                    return null;
                }
                java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
                siteApiHourlyResponse = new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
            }

        } catch (Exception e) {
            return null;
        }
        return siteApiHourlyResponse;
    }

    @Test
    public void testSiteApiFeatureCollectionHourly() {
      SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(getSiteHourlyApiResponse(),
                SiteApiFeatureCollection.class);
        if (response != null) {
            assertEquals(SiteApiFeatureCollection.TYPE_SITE_API_FEATURE_COLLECTION, response.getType());
        } else {
            fail("GSON returned null");
        }
    }

    @Test
    public void testSiteApiFeatureCollectionDaily() {
        SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(getSiteDailyApiResponse(),
                SiteApiFeatureCollection.class);
        if (response != null) {
            assertEquals(SiteApiFeatureCollection.TYPE_SITE_API_FEATURE_COLLECTION, response.getType());
        } else {
            fail("GSON returned null");
        }
    }

    @Test
    public void testSiteApiFeatureHourly() {
        SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(getSiteHourlyApiResponse(),
                SiteApiFeatureCollection.class);
        assertNotNull(response);
        assertNotNull(response.getFeature());
        assertEquals(1, response.getFeature().length);
        assertEquals(SiteApiFeature.TYPE_SITE_API_FEATURE, response.getFeature()[0].getType());
    }

    @Test
    public void testSiteApiFeatureDaily() {
        SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(getSiteDailyApiResponse(),
                SiteApiFeatureCollection.class);
        assertNotNull(response);
        assertNotNull(response.getFeature());
        assertEquals(1,response.getFeature().length);
        assertEquals(SiteApiFeature.TYPE_SITE_API_FEATURE, response.getFeature()[0].getType());
    }

    @Test
    public void testSiteApiFeatureGeometryHourly() {
        SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(getSiteHourlyApiResponse(),
                SiteApiFeatureCollection.class);
        assertNotNull(response);
        assertNotNull(response.getFeature());
        assertEquals(1, response.getFeature().length);
        assertEquals(SiteApiFeaturePoint.TYPE_SITE_API_FEATURE, response.getFeature()[0].getGeometry().getType());
        assertEquals(-0.32430000000000003,response.getFeature()[0].getGeometry().getLongitude());
        assertEquals(51.0624,response.getFeature()[0].getGeometry().getLatitude());
        assertEquals(50.0,response.getFeature()[0].getGeometry().getElevation());
    }

    @Test
    public void testSiteApiFeatureGeometryDaily() {
        SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(getSiteDailyApiResponse(),
                SiteApiFeatureCollection.class);

        assertNotNull(response);
        assertNotNull(response.getFeature());
        assertEquals(1, response.getFeature().length);
        assertEquals(SiteApiFeaturePoint.TYPE_SITE_API_FEATURE, response.getFeature()[0].getGeometry().getType());
        assertEquals(-0.32430000000000003,response.getFeature()[0].getGeometry().getLongitude());
        assertEquals(51.0624,response.getFeature()[0].getGeometry().getLatitude());
        assertEquals(50.0,response.getFeature()[0].getGeometry().getElevation());
    }

    @Test
    public void testSiteApiFeaturePropertiesHourly() {
        SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(getSiteHourlyApiResponse(),
                SiteApiFeatureCollection.class);

        assertNotNull(response);
        assertNotNull(response.getFeature());
        assertEquals(1, response.getFeature().length);
        assertEquals("Horsham",response.getFeature()[0].getProperties().getLocation().getName());
        assertEquals(0.1508,response.getFeature()[0].getProperties().getRequestPointDistance());
        assertEquals("2022-09-17T20:00Z",response.getFeature()[0].getProperties().getModelRunDate());
    }

    @Test
    public void testSiteApiFeaturePropertiesDaily() {
        SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(getSiteDailyApiResponse(),
                SiteApiFeatureCollection.class);

        assertNotNull(response);
        assertNotNull(response.getFeature());
        assertEquals(1, response.getFeature().length);
        assertEquals("Horsham",response.getFeature()[0].getProperties().getLocation().getName());
        assertEquals(0.1508,response.getFeature()[0].getProperties().getRequestPointDistance());
        assertEquals("2022-09-19T21:00Z",response.getFeature()[0].getProperties().getModelRunDate());
    }

    @Test
    public void testSiteApiFeaturePropertiesTimeSeries0Hourly() {
        SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(getSiteHourlyApiResponse(),
                SiteApiFeatureCollection.class);

        assertNotNull(response);
        assertNotNull(response.getFeature());
        assertEquals(1, response.getFeature().length);
        assertEquals("2022-09-17T20:00Z",response.getFeature()[0].getProperties().getTimeSeries()[0].getTime());
        assertEquals( 8.55,response.getFeature()[0].getProperties().getTimeSeries()[0].getScreenTemperature());
        assertEquals( 10.36,response.getFeature()[0].getProperties().getTimeSeries()[0].getMaxScreenTemperature());
        assertEquals( 8.54,response.getFeature()[0].getProperties().getTimeSeries()[0].getMinScreenTemperature());
        assertEquals( 4.67,response.getFeature()[0].getProperties().getTimeSeries()[0].getScreenDewPointTemperature());
        assertEquals( 8.18,response.getFeature()[0].getProperties().getTimeSeries()[0].getFeelsLikeTemperature());
        assertEquals( 0.46,response.getFeature()[0].getProperties().getTimeSeries()[0].getWindSpeed10m());
        assertEquals( 297,response.getFeature()[0].getProperties().getTimeSeries()[0].getWindDirectionFrom10m());
        assertEquals( 4.63,response.getFeature()[0].getProperties().getTimeSeries()[0].getWindGustSpeed10m());
        assertEquals( 6.43,response.getFeature()[0].getProperties().getTimeSeries()[0].getMax10mWindGust());
        assertEquals( 19040,response.getFeature()[0].getProperties().getTimeSeries()[0].getVisibility());
        assertEquals( 76.51,response.getFeature()[0].getProperties().getTimeSeries()[0].getScreenRelativeHumidity());
        assertEquals( 102230,response.getFeature()[0].getProperties().getTimeSeries()[0].getPressure());
        assertEquals( 1,response.getFeature()[0].getProperties().getTimeSeries()[0].getUvIndex());
        assertEquals( 2,response.getFeature()[0].getProperties().getTimeSeries()[0].getSignificantWeatherCode());
        assertEquals( 3,response.getFeature()[0].getProperties().getTimeSeries()[0].getPrecipitationRate());
        assertEquals( 4,response.getFeature()[0].getProperties().getTimeSeries()[0].getTotalPrecipAmount());
        assertEquals( 5,response.getFeature()[0].getProperties().getTimeSeries()[0].getTotalSnowAmount());
        assertEquals( 60,response.getFeature()[0].getProperties().getTimeSeries()[0].getProbOfPrecipitation());
    }

    @Test
    public void testSiteApiFeaturePropertiesTimeSeries0Daily() {
        SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(getSiteDailyApiResponse(),
                SiteApiFeatureCollection.class);

        assertNotNull(response);
        assertNotNull(response.getFeature());
        assertEquals(1, response.getFeature().length);
        assertEquals("2022-09-18T00:00Z",response.getFeature()[0].getProperties().getTimeSeries()[0].getTime());
        assertEquals( 1.39,response.getFeature()[0].getProperties().getTimeSeries()[0].getMidnight10MWindSpeed());
        assertEquals( 31,response.getFeature()[0].getProperties().getTimeSeries()[0].getMidnight10MWindDirection());
        assertEquals( 5.66,response.getFeature()[0].getProperties().getTimeSeries()[0].getMidnight10MWindGust());
        assertEquals( 8776,response.getFeature()[0].getProperties().getTimeSeries()[0].getMidnightVisibility());
        assertEquals( 91.42,response.getFeature()[0].getProperties().getTimeSeries()[0].getMidnightRelativeHumidity());
        assertEquals( 102310,response.getFeature()[0].getProperties().getTimeSeries()[0].getMidnightPressure());
        assertEquals( 2,response.getFeature()[0].getProperties().getTimeSeries()[0].getNightSignificantWeatherCode());
        assertEquals( 8.05,response.getFeature()[0].getProperties().getTimeSeries()[0].getNightMinScreenTemperature());
        assertEquals( 12.79,response.getFeature()[0].getProperties().getTimeSeries()[0].getNightUpperBoundMinTemp());
        assertEquals( 5.51,response.getFeature()[0].getProperties().getTimeSeries()[0].getNightLowerBoundMinTemp());
        assertEquals( 7.35,response.getFeature()[0].getProperties().getTimeSeries()[0].getNightMinFeelsLikeTemp());
        assertEquals( 12.83,response.getFeature()[0].getProperties().getTimeSeries()[0].getNightUpperBoundMinFeelsLikeTemp());
        assertEquals( 7.33,response.getFeature()[0].getProperties().getTimeSeries()[0].getNightLowerBoundMinFeelsLikeTemp());
        assertEquals( 5,response.getFeature()[0].getProperties().getTimeSeries()[0].getNightProbabilityOfPrecipitation());
        assertEquals( 0,response.getFeature()[0].getProperties().getTimeSeries()[0].getNightProbabilityOfSnow());
        assertEquals( 1,response.getFeature()[0].getProperties().getTimeSeries()[0].getNightProbabilityOfHeavySnow());
        assertEquals( 5,response.getFeature()[0].getProperties().getTimeSeries()[0].getNightProbabilityOfRain());
        assertEquals( 3,response.getFeature()[0].getProperties().getTimeSeries()[0].getNightProbabilityOfHeavyRain());
        assertEquals( 4,response.getFeature()[0].getProperties().getTimeSeries()[0].getNightProbabilityOfHail());
        assertEquals( 6,response.getFeature()[0].getProperties().getTimeSeries()[0].getNightProbabilityOfSferics());
    }

    @Test
    public void testGetTimeSeriesForCurrentHour() {
        SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(getSiteHourlyApiResponse(),
                SiteApiFeatureCollection.class);

        assertNotNull(response);
        assertNotNull(response.getFeature());
        assertEquals(1, response.getFeature().length);
        assertEquals( 0,response.getFeature()[0].getProperties().getHourlyTimeSeriesPositionForCurrentHour("2022-09-17T20:00Z"));
        assertEquals( 1,response.getFeature()[0].getProperties().getHourlyTimeSeriesPositionForCurrentHour("2022-09-17T21:00Z"));
        assertEquals( 2,response.getFeature()[0].getProperties().getHourlyTimeSeriesPositionForCurrentHour("2022-09-17T22:00Z"));
        assertEquals( 3,response.getFeature()[0].getProperties().getHourlyTimeSeriesPositionForCurrentHour("2022-09-17T23:00Z"));
        assertEquals( 24,response.getFeature()[0].getProperties().getHourlyTimeSeriesPositionForCurrentHour("2022-09-18T20:00Z"));
        assertEquals( 48,response.getFeature()[0].getProperties().getHourlyTimeSeriesPositionForCurrentHour("2022-09-19T20:00Z"));
    }

}
