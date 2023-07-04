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
package org.openhab.binding.boschshc.internal.devices.camera;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openhab.binding.boschshc.internal.devices.AbstractBoschSHCDeviceHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.cameranotification.CameraNotificationState;
import org.openhab.binding.boschshc.internal.services.cameranotification.dto.CameraNotificationServiceState;
import org.openhab.binding.boschshc.internal.services.privacymode.PrivacyModeState;
import org.openhab.binding.boschshc.internal.services.privacymode.dto.PrivacyModeServiceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link CameraHandler}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class CameraHandlerTest extends AbstractBoschSHCDeviceHandlerTest<CameraHandler> {

    private @Captor @NonNullByDefault({}) ArgumentCaptor<PrivacyModeServiceState> privacyModeServiceStateCaptor;

    private @Captor @NonNullByDefault({}) ArgumentCaptor<CameraNotificationServiceState> cameraNotificationServiceStateCaptor;

    @Override
    protected CameraHandler createFixture() {
        return new CameraHandler(getThing());
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_CAMERA_360;
    }

    @Override
    protected String getDeviceID() {
        return "8e28ce2d-e7bf-3e3d-8e3a-a78de61b493e";
    }

    @Test
    void testHandleCommandPrivacyMode()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_PRIVACY_MODE),
                OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("PrivacyMode"),
                privacyModeServiceStateCaptor.capture());
        PrivacyModeServiceState state = privacyModeServiceStateCaptor.getValue();
        assertSame(PrivacyModeState.ENABLED, state.value);

        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_PRIVACY_MODE),
                OnOffType.OFF);
        verify(getBridgeHandler(), times(2)).putState(eq(getDeviceID()), eq("PrivacyMode"),
                privacyModeServiceStateCaptor.capture());
        state = privacyModeServiceStateCaptor.getValue();
        assertSame(PrivacyModeState.DISABLED, state.value);
    }

    @Test
    void testHandleCommandCameraNotification()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CAMERA_NOTIFICATION),
                OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("CameraNotification"),
                cameraNotificationServiceStateCaptor.capture());
        CameraNotificationServiceState state = cameraNotificationServiceStateCaptor.getValue();
        assertSame(CameraNotificationState.ENABLED, state.value);

        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CAMERA_NOTIFICATION),
                OnOffType.OFF);
        verify(getBridgeHandler(), times(2)).putState(eq(getDeviceID()), eq("CameraNotification"),
                cameraNotificationServiceStateCaptor.capture());
        state = cameraNotificationServiceStateCaptor.getValue();
        assertSame(CameraNotificationState.DISABLED, state.value);
    }

    @Test
    void testUpdateChannelsPrivacyModeState() {
        JsonElement jsonObject = JsonParser.parseString("{\"@type\":\"privacyModeState\",\"value\":\"ENABLED\"}");
        getFixture().processUpdate("PrivacyMode", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_PRIVACY_MODE), OnOffType.ON);

        jsonObject = JsonParser.parseString("{\"@type\":\"privacyModeState\",\"value\":\"DISABLED\"}");
        getFixture().processUpdate("PrivacyMode", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_PRIVACY_MODE), OnOffType.OFF);
    }

    @Test
    void testUpdateChannelsCameraNotificationState() {
        JsonElement jsonObject = JsonParser
                .parseString("{\"@type\":\"cameraNotificationState\",\"value\":\"ENABLED\"}");
        getFixture().processUpdate("CameraNotification", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CAMERA_NOTIFICATION),
                OnOffType.ON);

        jsonObject = JsonParser.parseString("{\"@type\":\"cameraNotificationState\",\"value\":\"DISABLED\"}");
        getFixture().processUpdate("CameraNotification", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CAMERA_NOTIFICATION),
                OnOffType.OFF);
    }
}
