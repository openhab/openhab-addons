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
package org.openhab.binding.bmwcardata.internal.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openhab.binding.bmwcardata.internal.BMWCarDataBindingConstants.THING_TYPE_ACCOUNT;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataException;
import org.openhab.binding.bmwcardata.internal.api.dto.BasicData;
import org.openhab.binding.bmwcardata.internal.api.dto.VehicleMapping;
import org.openhab.binding.bmwcardata.internal.handler.BMWCarDataAccountHandler;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

/**
 * Tests the labels of the vehicles found by the {@link BMWCarDataVehicleDiscoveryService}.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
class BMWCarDataVehicleDiscoveryServiceTest {

    private static final String VIN = "WBY11CF020CM00000";
    private static final ThingUID BRIDGE_UID = new ThingUID(THING_TYPE_ACCOUNT, "account");

    private @NonNullByDefault({}) BMWCarDataAccountHandler handler;
    private @NonNullByDefault({}) DiscoveryListener listener;
    private @NonNullByDefault({}) BMWCarDataVehicleDiscoveryService service;

    @BeforeEach
    void setUp() throws Exception {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(BRIDGE_UID);
        handler = mock(BMWCarDataAccountHandler.class);
        when(handler.getThing()).thenReturn(bridge);
        VehicleMapping mapping = new VehicleMapping();
        mapping.vin = VIN;
        mapping.mappingType = VehicleMapping.MAPPING_TYPE_PRIMARY;
        when(handler.fetchVehicleMappings()).thenReturn(List.of(mapping));

        listener = mock(DiscoveryListener.class);
        service = new BMWCarDataVehicleDiscoveryService();
        service.setThingHandler(handler);
        service.addDiscoveryListener(listener);
    }

    @Test
    void labelHasBrandModelAndVin() throws Exception {
        when(handler.fetchBasicData(VIN)).thenReturn(basicData(true));
        assertEquals("BMW iX xDrive40 (" + VIN + ")", discoveredLabel());
    }

    @Test
    void labelWithoutBasicDataIsTheVin() throws Exception {
        when(handler.fetchBasicData(VIN)).thenThrow(new BMWCarDataException("Request budget used up"));
        assertEquals(VIN, discoveredLabel());
    }

    @Test
    void missingFlagIsNotMarked() throws Exception {
        when(handler.fetchBasicData(VIN)).thenReturn(basicData(null));
        assertEquals("BMW iX xDrive40 (" + VIN + ")", discoveredLabel());
    }

    @Test
    void falseFlagIsMarked() throws Exception {
        when(handler.fetchBasicData(VIN)).thenReturn(basicData(false));
        assertEquals("@text/discovery.vehicle.no-telematics.label [\"BMW iX xDrive40 (" + VIN + ")\"]",
                discoveredLabel());
    }

    private String discoveredLabel() {
        service.discoverVehicles();
        // the listeners are notified asynchronously
        ArgumentCaptor<DiscoveryResult> result = ArgumentCaptor.forClass(DiscoveryResult.class);
        verify(listener, timeout(5000)).thingDiscovered(eq(service), result.capture());
        return result.getValue().getLabel();
    }

    private static BasicData basicData(@Nullable Boolean telematicsCapable) {
        BasicData basicData = new BasicData();
        basicData.brand = "BMW";
        basicData.modelName = "iX xDrive40";
        basicData.isTelematicsCapable = telematicsCapable;
        return basicData;
    }
}
