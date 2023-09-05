/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.fmiweather;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.fmiweather.internal.client.FMISID;
import org.openhab.binding.fmiweather.internal.client.ForecastRequest;
import org.openhab.binding.fmiweather.internal.client.LatLon;
import org.openhab.binding.fmiweather.internal.client.ObservationRequest;
import org.openhab.binding.fmiweather.internal.client.QueryParameter;

/**
 * Tests for converting Request objects to URLs
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class FMIRequestTest {

    @Test
    public void testObservationRequestToUrl() {
        ObservationRequest request = new ObservationRequest(new FMISID("101023"), 1552215664L, 1552215665L, 61);
        assertThat(request.toUrl(),
                is("""
                        https://opendata.fmi.fi/wfs?service=WFS&version=2.0.0&request=getFeature&storedquery_id=fmi::observations::weather::multipointcoverage\
                        &starttime=2019-03-10T11:01:04Z&endtime=2019-03-10T11:01:05Z&timestep=61&fmisid=101023\
                        &parameters=t2m,rh,wd_10min,ws_10min,wg_10min,p_sea,r_1h,snow_aws,vis,n_man,wawa\
                        """));
    }

    @Test
    public void testForecastRequestToUrl() {
        ForecastRequest request = new ForecastRequest(new LatLon(new BigDecimal("9"), new BigDecimal("8")), 1552215664L,
                1552215665L, 61);
        assertThat(request.toUrl(),
                is("""
                        https://opendata.fmi.fi/wfs?service=WFS&version=2.0.0&request=getFeature&storedquery_id=fmi::forecast::harmonie::surface::point::multipointcoverage\
                        &starttime=2019-03-10T11:01:04Z&endtime=2019-03-10T11:01:05Z&timestep=61&latlon=9,8\
                        &parameters=Temperature,Humidity,WindDirection,WindSpeedMS,WindGust,Pressure,Precipitation1h,TotalCloudCover,WeatherSymbol3\
                        """));
    }

    @Test
    public void testCustomLocation() {
        QueryParameter location = new QueryParameter() {

            @Override
            public List<Entry<String, String>> toRequestParameters() {
                return Arrays.asList(new AbstractMap.SimpleImmutableEntry<>("lat", "MYLAT"),
                        new AbstractMap.SimpleImmutableEntry<>("lon", "FOO"),
                        new AbstractMap.SimpleImmutableEntry<>("special", "x,y,z"));
            }
        };
        ObservationRequest request = new ObservationRequest(location, 1552215664L, 1552215665L, 61);
        assertThat(request.toUrl(),
                is("""
                        https://opendata.fmi.fi/wfs?service=WFS&version=2.0.0&request=getFeature&storedquery_id=fmi::observations::weather::multipointcoverage\
                        &starttime=2019-03-10T11:01:04Z&endtime=2019-03-10T11:01:05Z&timestep=61&lat=MYLAT&lon=FOO&special=x,y,z\
                        &parameters=t2m,rh,wd_10min,ws_10min,wg_10min,p_sea,r_1h,snow_aws,vis,n_man,wawa\
                        """));
    }
}
