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
 * Proves the tolerant BootNotification handling: the embedded library rejects an inbound boot whose
 * {@code chargePointModel}/{@code chargePointVendor} breaks the OCPP CiString20 constraint, which
 * bricks the charger; the tolerant type accepts it while keeping the values for the Thing properties,
 * and the tolerant feature overrides the core one.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class TolerantBootNotificationTest {

    // 25 characters — over the OCPP CiString20 limit. A representative over-length model; the library
    // rejects ANY model/vendor beyond 20, so this stands in for whatever the real charger sends.
    private static final String LONG_MODEL = "Eve Single Pro-line 22 kW";
    private static final String VENDOR = "Alfen BV";
    private static final String BOOT_JSON = "{\"chargePointVendor\":\"" + VENDOR + "\",\"chargePointModel\":\""
            + LONG_MODEL + "\"}";

    // BootNotificationRequest has only String fields, so this deserializes identically to the library's
    // Gson (whose sole customisation is a ZonedDateTime adapter, irrelevant here). Field reflection sets
    // the value directly, bypassing the length-checking setter, exactly as JSONCommunicator does.
    private static final Gson GSON = new Gson();

    @Test
    void theLibraryRejectsAnOverLongModel() {
        // Reproduces the bug: the strict library type fails validation on a >20 model, which is what
        // makes the library answer OccurenceConstraintViolation and the charger never boot.
        assertTrue(LONG_MODEL.length() > 20);
        BootNotificationRequest strict = GSON.fromJson(BOOT_JSON, BootNotificationRequest.class);
        assertFalse(strict.validate(), "the embedded library rejects a model over 20 chars");
    }

    @Test
    void theTolerantRequestAcceptsAnOverLongModelAndKeepsIt() {
        // The fix: deserialized the same way, the tolerant type validates and retains the over-long
        // model (and vendor) for the Thing properties.
        TolerantBootNotificationRequest tolerant = GSON.fromJson(BOOT_JSON, TolerantBootNotificationRequest.class);
        assertTrue(tolerant.validate());
        assertEquals(LONG_MODEL, tolerant.getChargePointModel());
        assertEquals(VENDOR, tolerant.getChargePointVendor());
    }

    @Test
    void theTolerantRequestAlsoAcceptsAMissingModel() {
        // The other validate()-fail case: an omitted model. Still accepted, so the boot is not refused.
        TolerantBootNotificationRequest tolerant = GSON.fromJson("{\"chargePointVendor\":\"" + VENDOR + "\"}",
                TolerantBootNotificationRequest.class);
        assertTrue(tolerant.validate());
    }

    @Test
    void theTolerantFeatureOverridesTheCoreBootNotificationFeature() {
        // A later addFeature wins on the action, so the server deserializes an inbound BootNotification
        // into the tolerant type. Uses the real library FeatureRepository + core profile.
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
        // Once accepted, the boot is handled exactly as the core feature would have handled it.
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
