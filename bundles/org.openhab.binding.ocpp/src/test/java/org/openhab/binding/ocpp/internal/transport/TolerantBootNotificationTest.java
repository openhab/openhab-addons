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
package org.openhab.binding.ocpp.internal.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import eu.chargetime.ocpp.FeatureRepository;
import eu.chargetime.ocpp.feature.Feature;
import eu.chargetime.ocpp.feature.profile.ServerCoreEventHandler;
import eu.chargetime.ocpp.feature.profile.ServerCoreProfile;
import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.core.BootNotificationConfirmation;
import eu.chargetime.ocpp.model.core.BootNotificationRequest;

/**
 * Tests that the tolerant BootNotification type and feature accept a boot the strict library rejects.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class TolerantBootNotificationTest {

    // 25 characters — over the OCPP CiString20 limit the library enforces on model/vendor.
    private static final String LONG_MODEL = "Eve Single Pro-line 22 kW";
    private static final String VENDOR = "Alfen BV";
    private static final String BOOT_JSON = "{\"chargePointVendor\":\"" + VENDOR + "\",\"chargePointModel\":\""
            + LONG_MODEL + "\"}";

    // Gson sets fields directly, bypassing the length-checking setter — exactly as the library's
    // JSONCommunicator deserializes an inbound request off the wire.
    private static final Gson GSON = new Gson();

    @Test
    void theLibraryRejectsAnOverLongModel() {
        assertTrue(LONG_MODEL.length() > 20);
        BootNotificationRequest strict = GSON.fromJson(BOOT_JSON, BootNotificationRequest.class);
        assertFalse(strict.validate(), "the embedded library rejects a model over 20 chars");
    }

    @Test
    void theTolerantRequestAcceptsAnOverLongModelAndKeepsIt() {
        TolerantBootNotificationRequest tolerant = GSON.fromJson(BOOT_JSON, TolerantBootNotificationRequest.class);
        assertTrue(tolerant.validate());
        assertEquals(LONG_MODEL, tolerant.getChargePointModel());
        assertEquals(VENDOR, tolerant.getChargePointVendor());
    }

    @Test
    void theTolerantRequestAlsoAcceptsAMissingModel() {
        TolerantBootNotificationRequest tolerant = GSON.fromJson("{\"chargePointVendor\":\"" + VENDOR + "\"}",
                TolerantBootNotificationRequest.class);
        assertTrue(tolerant.validate());
    }

    @Test
    void theTolerantFeatureOverridesTheCoreBootNotificationFeature() {
        // In the library's FeatureRepository a later addFeature wins on the action, so the tolerant
        // feature must resolve over the core profile's.
        ServerCoreEventHandler handler = mock(ServerCoreEventHandler.class);
        FeatureRepository repository = new FeatureRepository();
        repository.addFeatureProfile(new ServerCoreProfile(handler));
        repository.addFeature(new TolerantBootNotificationFeature(handler));

        Feature feature = repository.findFeature("BootNotification").orElseThrow();
        assertEquals(TolerantBootNotificationRequest.class, feature.getRequestType(),
                "the tolerant feature must be the one resolved for BootNotification");
    }

    @Test
    void theTolerantFeatureDelegatesToTheCoreHandler() {
        ServerCoreEventHandler handler = mock(ServerCoreEventHandler.class);
        BootNotificationConfirmation confirmation = new BootNotificationConfirmation();
        when(handler.handleBootNotificationRequest(any(), any())).thenReturn(confirmation);
        TolerantBootNotificationFeature feature = new TolerantBootNotificationFeature(handler);

        UUID session = UUID.randomUUID();
        TolerantBootNotificationRequest request = new TolerantBootNotificationRequest();
        Confirmation result = feature.handleRequest(session, request);

        assertEquals(confirmation, result);
        verify(handler).handleBootNotificationRequest(session, request);
    }
}
