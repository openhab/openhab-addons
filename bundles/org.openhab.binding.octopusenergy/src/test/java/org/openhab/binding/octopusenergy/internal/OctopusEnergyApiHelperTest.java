/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.octopusenergy.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.net.HttpURLConnection;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpContentResponse;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.octopusenergy.internal.dto.AccountProperty;
import org.openhab.binding.octopusenergy.internal.dto.ElectricityMeterPoint;
import org.openhab.binding.octopusenergy.internal.dto.GasMeterPoint;
import org.openhab.binding.octopusenergy.internal.exception.AuthenticationException;

/**
 * The {@link OctopusEnergyApiHelperTest} is a test class for {@link OctopusEnergyApiHelper}.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
class OctopusEnergyApiHelperTest {

    private static final String TEST_ACCOUNT_NUMBER = "A-ABCD1234";
    private static final String TEST_API_KEY_VALID = "sk_live_dshgHYyt46GJHDjd78SDFAdf";
    private static final String TEST_API_KEY_INVALID = "sk_live_xxxxxxxxxxxxxxxxxxxxxxxx";

    private static final int RESPONSE_200_STATUS = HttpURLConnection.HTTP_OK;
    private static final String RESPONSE_200_REASON = "OK";
    private static final int RESPONSE_401_STATUS = HttpURLConnection.HTTP_UNAUTHORIZED;
    private static final String RESPONSE_401_REASON = "NOT AUTHORIZED";
    private static final String RESPONSE_401_CONTENT = "";

    private static OctopusEnergyApiHelper apiHelper = new OctopusEnergyApiHelper();

    private static AuthenticationStore authenticationStore = mock(AuthenticationStore.class);
    private static HttpClient httpClient = mock(HttpClient.class);
    private static HttpRequest request = mock(HttpRequest.class);
    private static HttpContentResponse response = mock(HttpContentResponse.class);

    @BeforeAll
    static void setUp() throws Exception {
        when(httpClient.getAuthenticationStore()).thenReturn(authenticationStore);
        when(httpClient.newRequest(anyString())).thenReturn(request);
        when(request.method(HttpMethod.GET)).thenReturn(request);

        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(RESPONSE_200_STATUS);
        when(response.getReason()).thenReturn(RESPONSE_200_REASON);

        apiHelper.setHttpClient(httpClient);
        apiHelper.setApiKey(TEST_API_KEY_VALID);
        apiHelper.setAccountNumber(TEST_ACCOUNT_NUMBER);
    }

    @Test
    void testUpdateAccounts() throws Exception {
        when(response.getContentAsString()).thenReturn(
                "{\"number\":\"A-ABCD1234\",\"properties\":[{\"id\":2749233,\"moved_in_at\":\"2020-10-09T00:00:00+01:00\",\"moved_out_at\":null,\"address_line_1\":\"ADDRESS LINE 1\",\"address_line_2\":\"ADDRESS LINE 2\",\"address_line_3\":\"ADDRESS LINE 3\",\"town\":\"TOWN\",\"county\":\"COUNTY\",\"postcode\":\"POSTCODE\",\"electricity_meter_points\":[{\"mpan\":\"1019387543623\",\"profile_class\":1,\"consumption_standard\":4514,\"meters\":[{\"serial_number\":\"S81E028123\",\"registers\":[{\"identifier\":\"01\",\"rate\":\"STANDARD\",\"is_settlement_register\":true}]},{\"serial_number\":\"20L3251234\",\"registers\":[{\"identifier\":\"1\",\"rate\":\"STANDARD\",\"is_settlement_register\":true}]}],\"agreements\":[{\"tariff_code\":\"E-1R-SUPER-GREEN-12M-20-09-22-A\",\"valid_from\":\"2020-11-01T00:00:00Z\",\"valid_to\":\"2020-12-11T00:00:00Z\"},{\"tariff_code\":\"E-1R-AGILE-18-02-21-A\",\"valid_from\":\"2020-12-11T00:00:00Z\",\"valid_to\":\"2021-12-11T00:00:00Z\"}],\"is_export\":false}],\"gas_meter_points\":[{\"mprn\":\"3046829404\",\"consumption_standard\":23882,\"meters\":[{\"serial_number\":\"6326031\"}],\"agreements\":[{\"tariff_code\":\"G-1R-SUPER-GREEN-12M-2* Connection #0 to host api.octopus.energy left intact 0-09-22-A\",\"valid_from\":\"2020-11-01T00:00:00Z\",\"valid_to\":\"2021-11-01T00:00:00Z\"}]}]}]}");
        // when:
        apiHelper.updateAccounts();
        // then:
        assertEquals("A-ABCD1234", apiHelper.getAccounts().number);
        assertEquals(1, apiHelper.getAccounts().properties.size());
        assertEquals(1, apiHelper.getAccounts().getElectricityMeterPoints().size());
        assertEquals(1, apiHelper.getAccounts().getGasMeterPoints().size());

        AccountProperty property = apiHelper.getAccounts().properties.get(0);
        assertEquals(2749233, property.id);
        assertEquals(ZonedDateTime.parse("2020-10-09T00:00+01:00"), property.movedInAt);
        assertNull(property.movedOutAt);
        assertEquals("ADDRESS LINE 1", property.addressLine1);
        assertEquals("ADDRESS LINE 2", property.addressLine2);
        assertEquals("ADDRESS LINE 3", property.addressLine3);
        assertEquals("TOWN", property.town);
        assertEquals("COUNTY", property.county);
        assertEquals("POSTCODE", property.postcode);

        ElectricityMeterPoint emp = apiHelper.getAccounts().getElectricityMeterPoints().get(0);
        assertEquals("1019387543623", emp.mpan);

        GasMeterPoint gmp = apiHelper.getAccounts().getGasMeterPoints().get(0);
        assertEquals("3046829404", gmp.mprn);
    }

    @Test
    void testUpdateElectricityConsumption() throws Exception {
        when(response.getContentAsString()).thenReturn(
                "{\"number\":\"A-ABCD1234\",\"properties\":[{\"id\":2749233,\"moved_in_at\":\"2020-10-09T00:00:00+01:00\",\"moved_out_at\":null,\"address_line_1\":\"ADDRESS LINE 1\",\"address_line_2\":\"ADDRESS LINE 2\",\"address_line_3\":\"ADDRESS LINE 3\",\"town\":\"TOWN\",\"county\":\"COUNTY\",\"postcode\":\"POSTCODE\",\"electricity_meter_points\":[{\"mpan\":\"1019387543623\",\"profile_class\":1,\"consumption_standard\":4514,\"meters\":[{\"serial_number\":\"S81E028123\",\"registers\":[{\"identifier\":\"01\",\"rate\":\"STANDARD\",\"is_settlement_register\":true}]},{\"serial_number\":\"20L3251234\",\"registers\":[{\"identifier\":\"1\",\"rate\":\"STANDARD\",\"is_settlement_register\":true}]}],\"agreements\":[{\"tariff_code\":\"E-1R-SUPER-GREEN-12M-20-09-22-A\",\"valid_from\":\"2020-11-01T00:00:00Z\",\"valid_to\":\"2020-12-11T00:00:00Z\"},{\"tariff_code\":\"E-1R-AGILE-18-02-21-A\",\"valid_from\":\"2020-12-11T00:00:00Z\",\"valid_to\":\"2021-12-11T00:00:00Z\"}],\"is_export\":false}],\"gas_meter_points\":[{\"mprn\":\"3046829404\",\"consumption_standard\":23882,\"meters\":[{\"serial_number\":\"6326031\"}],\"agreements\":[{\"tariff_code\":\"G-1R-SUPER-GREEN-12M-2* Connection #0 to host api.octopus.energy left intact 0-09-22-A\",\"valid_from\":\"2020-11-01T00:00:00Z\",\"valid_to\":\"2021-11-01T00:00:00Z\"}]}]}]}");
        apiHelper.updateAccounts();

        when(response.getContentAsString()).thenReturn(
                "{\"count\":49,\"next\":\"https://api.octopus.energy/v1/electricity-meter-points/1019387543623/meters/20L3251234/consumption/?order_by=-period&page=2&page_size=10&period_from=2021-01-16T22%3A58%3A25Z&period_to=2021-01-18T22%3A58%3A25Z\",\"previous\":null,\"results\":[{\"consumption\":0.248,\"interval_start\":\"2021-01-17T23:00:00Z\",\"interval_end\":\"2021-01-17T23:30:00Z\"},{\"consumption\":0.305,\"interval_start\":\"2021-01-17T22:30:00Z\",\"interval_end\":\"2021-01-17T23:00:00Z\"},{\"consumption\":0.309,\"interval_start\":\"2021-01-17T22:00:00Z\",\"interval_end\":\"2021-01-17T22:30:00Z\"},{\"consumption\":0.287,\"interval_start\":\"2021-01-17T21:30:00Z\",\"interval_end\":\"2021-01-17T22:00:00Z\"},{\"consumption\":0.279,\"interval_start\":\"2021-01-17T21:00:00Z\",\"interval_end\":\"2021-01-17T21:30:00Z\"},{\"consumption\":0.283,\"interval_start\":\"2021-01-17T20:30:00Z\",\"interval_end\":\"2021-01-17T21:00:00Z\"},{\"consumption\":0.267,\"interval_start\":\"2021-01-17T20:00:00Z\",\"interval_end\":\"2021-01-17T20:30:00Z\"},{\"consumption\":0.266,\"interval_start\":\"2021-01-17T19:30:00Z\",\"interval_end\":\"2021-01-17T20:00:00Z\"},{\"consumption\":0.282,\"interval_start\":\"2021-01-17T19:00:00Z\",\"interval_end\":\"2021-01-17T19:30:00Z\"},{\"consumption\":0.686,\"interval_start\":\"2021-01-17T18:30:00Z\",\"interval_end\":\"2021-01-17T19:00:00Z\"}]}");

        // when:
        apiHelper.updateElectricityConsumption();
        // then:
        // 10 is the default page size
        assertEquals(10, apiHelper.getAccounts().getElectricityMeterPoints().get(0).consumptionList.size());
    }

    @Test
    void testUpdateElectricityPrices() throws Exception {
        when(response.getContentAsString()).thenReturn(
                "{\"number\":\"A-ABCD1234\",\"properties\":[{\"id\":2749233,\"moved_in_at\":\"2020-10-09T00:00:00+01:00\",\"moved_out_at\":null,\"address_line_1\":\"ADDRESS LINE 1\",\"address_line_2\":\"ADDRESS LINE 2\",\"address_line_3\":\"ADDRESS LINE 3\",\"town\":\"TOWN\",\"county\":\"COUNTY\",\"postcode\":\"POSTCODE\",\"electricity_meter_points\":[{\"mpan\":\"1019387543623\",\"profile_class\":1,\"consumption_standard\":4514,\"meters\":[{\"serial_number\":\"S81E028123\",\"registers\":[{\"identifier\":\"01\",\"rate\":\"STANDARD\",\"is_settlement_register\":true}]},{\"serial_number\":\"20L3251234\",\"registers\":[{\"identifier\":\"1\",\"rate\":\"STANDARD\",\"is_settlement_register\":true}]}],\"agreements\":[{\"tariff_code\":\"E-1R-SUPER-GREEN-12M-20-09-22-A\",\"valid_from\":\"2020-11-01T00:00:00Z\",\"valid_to\":\"2020-12-11T00:00:00Z\"},{\"tariff_code\":\"E-1R-AGILE-18-02-21-A\",\"valid_from\":\"2020-12-11T00:00:00Z\",\"valid_to\":\"2021-12-11T00:00:00Z\"}],\"is_export\":false}],\"gas_meter_points\":[{\"mprn\":\"3046829404\",\"consumption_standard\":23882,\"meters\":[{\"serial_number\":\"6326031\"}],\"agreements\":[{\"tariff_code\":\"G-1R-SUPER-GREEN-12M-2* Connection #0 to host api.octopus.energy left intact 0-09-22-A\",\"valid_from\":\"2020-11-01T00:00:00Z\",\"valid_to\":\"2021-11-01T00:00:00Z\"}]}]}]}");
        apiHelper.updateAccounts();

        when(response.getContentAsString()).thenReturn(
                "{\"count\":17,\"next\":null,\"previous\":null,\"results\":[{\"value_exc_vat\":10.92,\"value_inc_vat\":11.466,\"valid_from\":\"2021-01-19T22:30:00Z\",\"valid_to\":\"2021-01-19T23:00:00Z\"},{\"value_exc_vat\":12.18,\"value_inc_vat\":12.789,\"valid_from\":\"2021-01-19T22:00:00Z\",\"valid_to\":\"2021-01-19T22:30:00Z\"},{\"value_exc_vat\":11.76,\"value_inc_vat\":12.348,\"valid_from\":\"2021-01-19T21:30:00Z\",\"valid_to\":\"2021-01-19T22:00:00Z\"},{\"value_exc_vat\":13.44,\"value_inc_vat\":14.112,\"valid_from\":\"2021-01-19T21:00:00Z\",\"valid_to\":\"2021-01-19T21:30:00Z\"},{\"value_exc_vat\":12.18,\"value_inc_vat\":12.789,\"valid_from\":\"2021-01-19T20:30:00Z\",\"valid_to\":\"2021-01-19T21:00:00Z\"},{\"value_exc_vat\":13.23,\"value_inc_vat\":13.8915,\"valid_from\":\"2021-01-19T20:00:00Z\",\"valid_to\":\"2021-01-19T20:30:00Z\"},{\"value_exc_vat\":11.57,\"value_inc_vat\":12.1485,\"valid_from\":\"2021-01-19T19:30:00Z\",\"valid_to\":\"2021-01-19T20:00:00Z\"},{\"value_exc_vat\":14.28,\"value_inc_vat\":14.994,\"valid_from\":\"2021-01-19T19:00:00Z\",\"valid_to\":\"2021-01-19T19:30:00Z\"},{\"value_exc_vat\":27.91,\"value_inc_vat\":29.3055,\"valid_from\":\"2021-01-19T18:30:00Z\",\"valid_to\":\"2021-01-19T19:00:00Z\"},{\"value_exc_vat\":29.8,\"value_inc_vat\":31.29,\"valid_from\":\"2021-01-19T18:00:00Z\",\"valid_to\":\"2021-01-19T18:30:00Z\"},{\"value_exc_vat\":31.48,\"value_inc_vat\":33.054,\"valid_from\":\"2021-01-19T17:30:00Z\",\"valid_to\":\"2021-01-19T18:00:00Z\"},{\"value_exc_vat\":31.07,\"value_inc_vat\":32.6235,\"valid_from\":\"2021-01-19T17:00:00Z\",\"valid_to\":\"2021-01-19T17:30:00Z\"},{\"value_exc_vat\":30.01,\"value_inc_vat\":31.5105,\"valid_from\":\"2021-01-19T16:30:00Z\",\"valid_to\":\"2021-01-19T17:00:00Z\"},{\"value_exc_vat\":26.46,\"value_inc_vat\":27.783,\"valid_from\":\"2021-01-19T16:00:00Z\",\"valid_to\":\"2021-01-19T16:30:00Z\"},{\"value_exc_vat\":12.18,\"value_inc_vat\":12.789,\"valid_from\":\"2021-01-19T15:30:00Z\",\"valid_to\":\"2021-01-19T16:00:00Z\"},{\"value_exc_vat\":11.55,\"value_inc_vat\":12.1275,\"valid_from\":\"2021-01-19T15:00:00Z\",\"valid_to\":\"2021-01-19T15:30:00Z\"},{\"value_exc_vat\":11.63,\"value_inc_vat\":12.2115,\"valid_from\":\"2021-01-19T14:30:00Z\",\"valid_to\":\"2021-01-19T15:00:00Z\"}]}");

        // when:
        apiHelper.updateElectricityPrices();
        // then:
        assertEquals(17, apiHelper.getAccounts().getElectricityMeterPoints().get(0).priceList.size());
    }

    @Test
    void testUpdateAccountsInvalidApiKey() throws Exception {
        when(response.getStatus()).thenReturn(RESPONSE_401_STATUS);
        when(response.getReason()).thenReturn(RESPONSE_401_REASON);

        when(response.getContentAsString()).thenReturn(RESPONSE_401_CONTENT);
        apiHelper.setApiKey(TEST_API_KEY_INVALID);

        // then:
        // we want an authentication exception
        assertThrows(AuthenticationException.class, () -> {
            apiHelper.updateAccounts();
        });

        when(response.getStatus()).thenReturn(RESPONSE_200_STATUS);
        when(response.getReason()).thenReturn(RESPONSE_200_REASON);
    }
}
