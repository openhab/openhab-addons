/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.mercedesme.internal.api;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.config.AccountConfiguration;
import org.openhab.binding.mercedesme.internal.utils.Utils;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.test.storage.VolatileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.mbcarkit.proto.VehicleEvents.VEPUpdate;

/**
 * {@link TestUpdate} to get a vehicle update from the Mercedes servers
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TestUpdate implements AccessTokenRefreshListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestUpdate.class);

    private static HttpClient httpClient = new HttpClient(new SslContextFactory.Client());

    // NOTE: This is a manual test utility. Update the following placeholder values
    // with valid data from your environment before running:
    // - EMAIL: your Mercedes me account email address
    // - VIN: the vehicle identification number to query
    // - STORAGE: the storage key from jsondb (mercedesme.json) associated with the account
    private static final String EMAIL = "YOUR_EMAIL_ADDRESS";
    private static final String VIN = "YOUR_VIN";
    private static final String STORAGE = "YOUR_STORAGE_FROM_JSONDB_MERCEDESME.JSON";

    public static void main(String[] args) {
        LocaleProvider localeProvider = new LocaleProvider() {
            @Override
            public Locale getLocale() {
                return Locale.GERMANY;
            }
        };
        VolatileStorage<String> store = new VolatileStorage<>();
        store.put(EMAIL, STORAGE);
        AccountConfiguration ac = new AccountConfiguration();
        ac.region = "EU";
        ac.email = EMAIL;

        try {
            httpClient.start();
            RestApi api = new RestApi(new TestUpdate(), httpClient, ac, localeProvider, store);
            VEPUpdate vepUpdate = api.restGetVehicleAttributes(VIN);
            LOGGER.warn(Utils.proto2Json(vepUpdate, Constants.THING_TYPE_BEV));
        } catch (Exception e) {
            LOGGER.error("Error during test update", e.getMessage());
        } finally {
            try {
                httpClient.stop();
                httpClient.destroy();
            } catch (Exception e) {
                LOGGER.error("Error during test update", e.getMessage());
            }
        }
    }

    @Override
    public void onAccessTokenResponse(AccessTokenResponse tokenResponse) {
        LOGGER.warn("Access token refreshed {}", tokenResponse.getAccessToken());
    }
}
