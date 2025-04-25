/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Device;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * Abstract unit test implementation for device handlers.
 *
 * @author David Pace - Initial contribution
 *
 * @param <T> type of the device handler to be tested
 */
@NonNullByDefault
public abstract class AbstractBoschSHCDeviceHandlerTest<T extends BoschSHCDeviceHandler>
        extends AbstractBoschSHCHandlerTest<T> {

    protected static final String TAG_LEGACY_LOCATION_PROPERTY = "LegacyLocationProperty";
    protected static final String TAG_LOCATION_PROPERTY = "LocationProperty";
    protected static final String DEFAULT_ROOM_ID = "hz_1";

    @Override
    protected void configureDevice(Device device) {
        super.configureDevice(device);

        device.id = getDeviceID();
        device.roomId = DEFAULT_ROOM_ID;
    }

    @Override
    protected void beforeHandlerInitialization(TestInfo testInfo) {
        super.beforeHandlerInitialization(testInfo);
        Set<String> tags = testInfo.getTags();
        if (tags.contains(TAG_LEGACY_LOCATION_PROPERTY) || tags.contains(TAG_LOCATION_PROPERTY)) {
            Map<String, String> properties = new HashMap<>();
            when(getThing().getProperties()).thenReturn(properties);

            if (tags.contains(TAG_LEGACY_LOCATION_PROPERTY)) {
                properties.put(BoschSHCBindingConstants.PROPERTY_LOCATION_LEGACY, "Living Room");
            }

            if (tags.contains(TAG_LOCATION_PROPERTY)) {
                when(getBridgeHandler().resolveRoomId(DEFAULT_ROOM_ID)).thenReturn("Kitchen");
            }
        }
    }

    @Override
    protected Configuration getConfiguration() {
        Configuration configuration = super.getConfiguration();
        configuration.put("id", getDeviceID());
        return configuration;
    }

    protected abstract String getDeviceID();

    @Test
    void initializeInvalidDeviceId() {
        getFixture().getThing().getConfiguration().remove("id");
        getFixture().initialize();

        verify(getCallback()).statusUpdated(any(Thing.class),
                argThat(status -> status.getStatus().equals(ThingStatus.OFFLINE)
                        && status.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
    }

    @ParameterizedTest
    @MethodSource("org.openhab.binding.boschshc.internal.tests.common.CommonTestUtils#getExecutionExceptionAndInterruptedExceptionArguments()")
    void initializeHandleExceptionDuringDeviceInfoRestCall(Exception exception)
            throws BoschSHCException, InterruptedException, TimeoutException, ExecutionException {
        when(getBridgeHandler().getDeviceInfo(getDeviceID())).thenThrow(exception);

        getFixture().initialize();

        verify(getCallback()).statusUpdated(any(Thing.class),
                argThat(status -> status.getStatus().equals(ThingStatus.OFFLINE)
                        && status.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
    }

    @Tag(TAG_LEGACY_LOCATION_PROPERTY)
    @Test
    protected void deleteLegacyLocationProperty() {
        verify(getThing()).setProperty(BoschSHCBindingConstants.PROPERTY_LOCATION_LEGACY, null);
        verify(getCallback()).thingUpdated(getThing());
    }

    @Tag(TAG_LOCATION_PROPERTY)
    @Test
    protected void locationPropertyDidNotChange() {
        verify(getThing()).setProperty(BoschSHCBindingConstants.PROPERTY_LOCATION, "Kitchen");
        verify(getCallback()).thingUpdated(getThing());

        getThing().getProperties().put(BoschSHCBindingConstants.PROPERTY_LOCATION, "Kitchen");

        // re-initialize
        getFixture().initialize();

        verify(getThing()).setProperty(BoschSHCBindingConstants.PROPERTY_LOCATION, "Kitchen");
        verify(getCallback()).thingUpdated(getThing());
    }

    @Tag(TAG_LOCATION_PROPERTY)
    @Test
    protected void locationPropertyDidChange() {
        verify(getThing()).setProperty(BoschSHCBindingConstants.PROPERTY_LOCATION, "Kitchen");
        verify(getCallback()).thingUpdated(getThing());

        getThing().getProperties().put(BoschSHCBindingConstants.PROPERTY_LOCATION, "Kitchen");

        getDevice().roomId = "hz_2";
        when(getBridgeHandler().resolveRoomId("hz_2")).thenReturn("Dining Room");

        // re-initialize
        getFixture().initialize();

        verify(getThing()).setProperty(BoschSHCBindingConstants.PROPERTY_LOCATION, "Dining Room");
        verify(getCallback(), times(2)).thingUpdated(getThing());
    }
}
