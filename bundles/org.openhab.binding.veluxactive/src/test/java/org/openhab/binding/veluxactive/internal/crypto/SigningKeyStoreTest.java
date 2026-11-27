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
package org.openhab.binding.veluxactive.internal.crypto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.veluxactive.internal.VeluxActiveBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.veluxactive.internal.VeluxActiveBindingConstants;
import org.openhab.binding.veluxactive.internal.exception.ApiException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;

/**
 * Verifies independent signature vectors, persisted replay counters and credential isolation.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
class SigningKeyStoreTest {
    private final StorageService service = mock(StorageService.class);
    private final ThingRegistry thingRegistry = mock(ThingRegistry.class);
    private final Map<String, String> data = new HashMap<>();
    private final SigningKey key = key();

    @BeforeEach
    void setup() {
        when(thingRegistry.getAll()).thenReturn(List.of());
        Storage<String> storage = mock();
        when(service.<String> getStorage(anyString(), any())).thenReturn(storage);
        when(storage.get(anyString())).thenAnswer(i -> data.get(i.getArgument(0)));
        when(storage.put(anyString(), anyString())).thenAnswer(i -> data.put(i.getArgument(0), i.getArgument(1)));
        when(storage.remove(anyString())).thenAnswer(i -> data.remove(i.getArgument(0)));
    }

    @Test
    void signsModernVectorAndKeepsCountersAcrossRecreationAndClockRollback() throws Exception {
        var store = new SigningKeyStore(service, thingRegistry);
        store.put("gateway", key);
        var signed = store.sign("gateway", "window", "target_position", 50, 1700000000L, true);
        assertThat(signed.get("hash_target_position"),
                is("qqTobwuOXPAkIsb0H6plGMXf6zDbQQuz4UeAcpc6NJLH233hbkjPhghfKCzsOWBlYIb5u_w92V7k2RpOOVkD2g=="));
        assertThat(signed.get("nonce"), is(0));
        var recreated = new SigningKeyStore(service, thingRegistry);
        var next = recreated.sign("gateway", "window", "target_position", 50, 1699999999L, true);
        assertThat(next.get("timestamp"), is(1700000000L));
        assertThat(next.get("nonce"), is(1));
        assertThat(recreated.sign("gateway", "window", "target_position", 50, 1700000001L, true).get("nonce"), is(0));
    }

    @Test
    void signsLegacyVectorWithoutNonceAndAdvancesTimestamp() throws Exception {
        var store = new SigningKeyStore(service, thingRegistry);
        store.put("gateway", key);
        var signed = store.sign("gateway", "gateway", "scenario", "home", 1700000000L, false);
        assertThat(signed.get("hash_scenario"),
                is("cOnOOSSZg69pDLNIbDma0TBboMDZ_o5b9smkBr8-MkiGFEtTwK5_8-0sHE44PF2qyEKbYfe7nxq7jWtDGJoRWA=="));
        assertThat(signed.containsKey("nonce"), is(false));
        assertThat(store.sign("gateway", "gateway", "scenario", "home", 1700000000L, false).get("timestamp"),
                is(1700000001L));
    }

    @Test
    void rollsNonceAtApkBoundary() throws Exception {
        var store = new SigningKeyStore(service, thingRegistry);
        store.put("gateway", key);
        store.sign("gateway", "window", "target_position", 50, 1700000000L, true);
        data.computeIfPresent("gateway", (id, json) -> json.replace("\"nonce\":0", "\"nonce\":32766"));
        var next = store.sign("gateway", "window", "target_position", 50, 1700000000L, true);
        assertThat(next.get("timestamp"), is(1700000001L));
        assertThat(next.get("nonce"), is(0));
    }

    @Test
    void isolatesGatewaysAndRejectsMissingOrMalformedCredentials() throws Exception {
        var store = new SigningKeyStore(service, thingRegistry);
        store.put("first", key);
        assertThrows(ApiException.class, () -> store.sign("second", "window", "target_position", 50, 1, true));
        data.put("second", "{}");
        assertThrows(ApiException.class, () -> store.sign("second", "window", "target_position", 50, 1, true));
        Thing owner = mock();
        ThingUID ownerUID = new ThingUID(THING_TYPE_GATEWAY, "removed");
        when(owner.getUID()).thenReturn(ownerUID);
        when(owner.getThingTypeUID()).thenReturn(THING_TYPE_GATEWAY);
        when(owner.getConfiguration())
                .thenReturn(new Configuration(Map.of(VeluxActiveBindingConstants.PARAMETER_MODULE_ID, "first")));
        when(thingRegistry.getAll()).thenReturn(List.of(owner));
        store.removeIfUnowned("first", ownerUID);
        assertThat(store.contains("first"), is(false));
        assertThat(key.toString(), is("SigningKey[redacted]"));
    }

    @Test
    void retainsCredentialWhileAnotherGatewayThingReferencesIt() {
        var store = new SigningKeyStore(service, thingRegistry);
        store.put("gateway", key);
        Thing otherGateway = mock();
        when(otherGateway.getUID()).thenReturn(new ThingUID(THING_TYPE_GATEWAY, "other"));
        when(otherGateway.getThingTypeUID()).thenReturn(THING_TYPE_GATEWAY);
        when(otherGateway.getConfiguration())
                .thenReturn(new Configuration(Map.of(VeluxActiveBindingConstants.PARAMETER_MODULE_ID, "gateway")));
        when(thingRegistry.getAll()).thenReturn(List.of(otherGateway));

        store.removeIfUnowned("gateway", new ThingUID(THING_TYPE_GATEWAY, "removed"));

        assertThat(store.contains("gateway"), is(true));
    }

    private static SigningKey key() {
        byte[] bytes = new byte[32];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }
        return new SigningKey(new byte[16], bytes);
    }
}
