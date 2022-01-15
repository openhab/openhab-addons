/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.presence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.netatmo.internal.NetatmoBindingConstants;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.internal.ThingImpl;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import io.swagger.client.model.NAWelcomeCamera;

/**
 * @author Sven Strohschein - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class NAPresenceCameraHandlerTest {

    private static final String DUMMY_VPN_URL = "https://dummytestvpnaddress.net/restricted/10.255.89.96/9826069dc689e8327ac3ed2ced4ff089/MTU5MTgzMzYwMDrQ7eHHhG0_OJ4TgmPhGlnK7QQ5pZ,,";
    private static final String DUMMY_LOCAL_URL = "http://192.168.178.76/9826069dc689e8327ac3ed2ced4ff089";
    private static final Optional<String> DUMMY_PING_RESPONSE = createPingResponseContent(DUMMY_LOCAL_URL);

    private @Mock RequestExecutor requestExecutorMock;
    private @Mock TimeZoneProvider timeZoneProviderMock;

    private Thing presenceCameraThing;
    private NAWelcomeCamera presenceCamera;
    private ChannelUID cameraStatusChannelUID;
    private ChannelUID floodlightChannelUID;
    private ChannelUID floodlightAutoModeChannelUID;
    private NAPresenceCameraHandlerAccessible handler;

    @BeforeEach
    public void before() {
        presenceCameraThing = new ThingImpl(new ThingTypeUID("netatmo", "NOC"), "1");
        presenceCamera = new NAWelcomeCamera();

        cameraStatusChannelUID = new ChannelUID(presenceCameraThing.getUID(),
                NetatmoBindingConstants.CHANNEL_CAMERA_STATUS);
        floodlightChannelUID = new ChannelUID(presenceCameraThing.getUID(),
                NetatmoBindingConstants.CHANNEL_CAMERA_FLOODLIGHT);
        floodlightAutoModeChannelUID = new ChannelUID(presenceCameraThing.getUID(),
                NetatmoBindingConstants.CHANNEL_CAMERA_FLOODLIGHT_AUTO_MODE);

        handler = new NAPresenceCameraHandlerAccessible(presenceCameraThing, presenceCamera);
    }

    @Test
    public void testHandleCommandSwitchSurveillanceOn() {
        when(requestExecutorMock.executeGETRequest(DUMMY_VPN_URL + "/command/ping")).thenReturn(DUMMY_PING_RESPONSE);

        presenceCamera.setVpnUrl(DUMMY_VPN_URL);
        handler.handleCommand(cameraStatusChannelUID, OnOffType.ON);

        verify(requestExecutorMock, times(2)).executeGETRequest(any()); // 1.) execute ping + 2.) execute switch on
        verify(requestExecutorMock).executeGETRequest(DUMMY_LOCAL_URL + "/command/changestatus?status=on");
    }

    @Test
    public void testHandleCommandSwitchSurveillanceOff() {
        when(requestExecutorMock.executeGETRequest(DUMMY_VPN_URL + "/command/ping")).thenReturn(DUMMY_PING_RESPONSE);

        presenceCamera.setVpnUrl(DUMMY_VPN_URL);
        handler.handleCommand(cameraStatusChannelUID, OnOffType.OFF);

        verify(requestExecutorMock, times(2)).executeGETRequest(any()); // 1.) execute ping + 2.) execute switch off
        verify(requestExecutorMock).executeGETRequest(DUMMY_LOCAL_URL + "/command/changestatus?status=off");
    }

    @Test
    public void testHandleCommandSwitchSurveillanceUnknownCommand() {
        presenceCamera.setVpnUrl(DUMMY_VPN_URL);
        handler.handleCommand(cameraStatusChannelUID, RefreshType.REFRESH);

        verify(requestExecutorMock, never()).executeGETRequest(any()); // nothing should get executed on a refresh
                                                                       // command
    }

    @Test
    public void testHandleCommandSwitchSurveillanceWithoutVPN() {
        handler.handleCommand(cameraStatusChannelUID, OnOffType.ON);

        verify(requestExecutorMock, never()).executeGETRequest(any()); // nothing should get executed when no VPN
                                                                       // address is set
    }

    @Test
    public void testHandleCommandSwitchFloodlightOn() {
        when(requestExecutorMock.executeGETRequest(DUMMY_VPN_URL + "/command/ping")).thenReturn(DUMMY_PING_RESPONSE);

        presenceCamera.setVpnUrl(DUMMY_VPN_URL);
        handler.handleCommand(floodlightChannelUID, OnOffType.ON);

        verify(requestExecutorMock, times(2)).executeGETRequest(any()); // 1.) execute ping + 2.) execute switch on
        verify(requestExecutorMock)
                .executeGETRequest(DUMMY_LOCAL_URL + "/command/floodlight_set_config?config=%7B%22mode%22:%22on%22%7D");
    }

    @Test
    public void testHandleCommandSwitchFloodlightOff() {
        when(requestExecutorMock.executeGETRequest(DUMMY_VPN_URL + "/command/ping")).thenReturn(DUMMY_PING_RESPONSE);

        presenceCamera.setVpnUrl(DUMMY_VPN_URL);
        handler.handleCommand(floodlightChannelUID, OnOffType.OFF);

        verify(requestExecutorMock, times(2)).executeGETRequest(any()); // 1.) execute ping + 2.) execute switch off
        verify(requestExecutorMock).executeGETRequest(
                DUMMY_LOCAL_URL + "/command/floodlight_set_config?config=%7B%22mode%22:%22off%22%7D");
    }

    @Test
    public void testHandleCommandSwitchFloodlightOffWithAutoModeOn() {
        when(requestExecutorMock.executeGETRequest(DUMMY_VPN_URL + "/command/ping")).thenReturn(DUMMY_PING_RESPONSE);

        presenceCamera.setVpnUrl(DUMMY_VPN_URL);
        presenceCamera.setLightModeStatus(NAWelcomeCamera.LightModeStatusEnum.AUTO);
        assertEquals(OnOffType.ON, handler.getNAThingProperty(floodlightAutoModeChannelUID.getId()));

        handler.handleCommand(floodlightChannelUID, OnOffType.OFF);

        verify(requestExecutorMock, times(2)).executeGETRequest(any()); // 1.) execute ping + 2.) execute switch off
        verify(requestExecutorMock).executeGETRequest(
                DUMMY_LOCAL_URL + "/command/floodlight_set_config?config=%7B%22mode%22:%22auto%22%7D");
    }

    @Test
    public void testHandleCommandSwitchFloodlightOnAddressChanged() {
        when(requestExecutorMock.executeGETRequest(DUMMY_VPN_URL + "/command/ping")).thenReturn(DUMMY_PING_RESPONSE);

        presenceCamera.setVpnUrl(DUMMY_VPN_URL);
        handler.handleCommand(floodlightChannelUID, OnOffType.ON);
        // 1.) execute ping + 2.) execute switch on
        verify(requestExecutorMock, times(2)).executeGETRequest(any());
        verify(requestExecutorMock)
                .executeGETRequest(DUMMY_LOCAL_URL + "/command/floodlight_set_config?config=%7B%22mode%22:%22on%22%7D");

        handler.handleCommand(floodlightChannelUID, OnOffType.OFF);
        // 1.) execute ping + 2.) execute switch on + 3.) execute switch off
        verify(requestExecutorMock, times(3)).executeGETRequest(any());
        verify(requestExecutorMock)
                .executeGETRequest(DUMMY_LOCAL_URL + "/command/floodlight_set_config?config=%7B%22mode%22:%22on%22%7D");
        verify(requestExecutorMock).executeGETRequest(
                DUMMY_LOCAL_URL + "/command/floodlight_set_config?config=%7B%22mode%22:%22off%22%7D");

        final String newDummyVPNURL = DUMMY_VPN_URL + "2";
        final String newDummyLocalURL = DUMMY_LOCAL_URL + "2";
        final Optional<String> newDummyPingResponse = createPingResponseContent(newDummyLocalURL);

        when(requestExecutorMock.executeGETRequest(newDummyVPNURL + "/command/ping")).thenReturn(newDummyPingResponse);

        presenceCamera.setVpnUrl(newDummyVPNURL);
        handler.handleCommand(floodlightChannelUID, OnOffType.ON);
        // 1.) execute ping + 2.) execute switch on + 3.) execute switch off + 4.) execute ping + 5.) execute switch on
        verify(requestExecutorMock, times(5)).executeGETRequest(any());
        verify(requestExecutorMock)
                .executeGETRequest(DUMMY_LOCAL_URL + "/command/floodlight_set_config?config=%7B%22mode%22:%22on%22%7D");
        verify(requestExecutorMock).executeGETRequest(
                DUMMY_LOCAL_URL + "/command/floodlight_set_config?config=%7B%22mode%22:%22off%22%7D");
        verify(requestExecutorMock).executeGETRequest(
                newDummyLocalURL + "/command/floodlight_set_config?config=%7B%22mode%22:%22on%22%7D");
    }

    @Test
    public void testHandleCommandSwitchFloodlightUnknownCommand() {
        presenceCamera.setVpnUrl(DUMMY_VPN_URL);
        handler.handleCommand(floodlightChannelUID, RefreshType.REFRESH);

        verify(requestExecutorMock, never()).executeGETRequest(any()); // nothing should get executed on a refresh
                                                                       // command
    }

    @Test
    public void testHandleCommandSwitchFloodlightAutoModeOn() {
        when(requestExecutorMock.executeGETRequest(DUMMY_VPN_URL + "/command/ping")).thenReturn(DUMMY_PING_RESPONSE);

        presenceCamera.setVpnUrl(DUMMY_VPN_URL);

        handler.handleCommand(floodlightAutoModeChannelUID, OnOffType.ON);

        verify(requestExecutorMock, times(2)).executeGETRequest(any()); // 1.) execute ping + 2.) execute switch
                                                                        // auto-mode on
        verify(requestExecutorMock).executeGETRequest(
                DUMMY_LOCAL_URL + "/command/floodlight_set_config?config=%7B%22mode%22:%22auto%22%7D");
    }

    @Test
    public void testHandleCommandSwitchFloodlightAutoModeOff() {
        when(requestExecutorMock.executeGETRequest(DUMMY_VPN_URL + "/command/ping")).thenReturn(DUMMY_PING_RESPONSE);

        presenceCamera.setVpnUrl(DUMMY_VPN_URL);

        handler.handleCommand(floodlightAutoModeChannelUID, OnOffType.OFF);

        verify(requestExecutorMock, times(2)).executeGETRequest(any()); // 1.) execute ping + 2.) execute switch off
        verify(requestExecutorMock).executeGETRequest(
                DUMMY_LOCAL_URL + "/command/floodlight_set_config?config=%7B%22mode%22:%22off%22%7D");
    }

    @Test
    public void testHandleCommandSwitchFloodlightAutoModeUnknownCommand() {
        presenceCamera.setVpnUrl(DUMMY_VPN_URL);
        handler.handleCommand(floodlightAutoModeChannelUID, RefreshType.REFRESH);

        verify(requestExecutorMock, never()).executeGETRequest(any()); // nothing should get executed on a refresh
                                                                       // command
    }

    /**
     * The request "fails" because there is no response content of the ping command.
     */
    @Test
    public void testHandleCommandRequestFailed() {
        presenceCamera.setVpnUrl(DUMMY_VPN_URL);
        handler.handleCommand(floodlightChannelUID, OnOffType.ON);

        verify(requestExecutorMock, times(1)).executeGETRequest(any()); // 1.) execute ping
    }

    @Test
    public void testHandleCommandWithoutVPN() {
        handler.handleCommand(floodlightChannelUID, OnOffType.ON);

        verify(requestExecutorMock, never()).executeGETRequest(any()); // no executions because the VPN URL is still
                                                                       // unknown
    }

    @Test
    public void testHandleCommandPingFailedNULLResponse() {
        when(requestExecutorMock.executeGETRequest(DUMMY_VPN_URL + "/command/ping")).thenReturn(Optional.of(""));

        presenceCamera.setVpnUrl(DUMMY_VPN_URL);
        handler.handleCommand(floodlightChannelUID, OnOffType.ON);

        verify(requestExecutorMock, times(1)).executeGETRequest(any()); // 1.) execute ping
    }

    @Test
    public void testHandleCommandPingFailedEmptyResponse() {
        when(requestExecutorMock.executeGETRequest(DUMMY_VPN_URL + "/command/ping")).thenReturn(Optional.of(""));

        presenceCamera.setVpnUrl(DUMMY_VPN_URL);
        handler.handleCommand(floodlightChannelUID, OnOffType.ON);

        verify(requestExecutorMock, times(1)).executeGETRequest(any()); // 1.) execute ping
    }

    @Test
    public void testHandleCommandPingFailedWrongResponse() {
        when(requestExecutorMock.executeGETRequest(DUMMY_VPN_URL + "/command/ping"))
                .thenReturn(Optional.of("{ \"message\":  \"error\" }"));

        presenceCamera.setVpnUrl(DUMMY_VPN_URL);
        handler.handleCommand(floodlightChannelUID, OnOffType.ON);

        verify(requestExecutorMock, times(1)).executeGETRequest(any()); // 1.) execute ping
    }

    @Test
    public void testHandleCommandModuleNULL() {
        NAPresenceCameraHandler handlerWithoutModule = new NAPresenceCameraHandler(presenceCameraThing,
                timeZoneProviderMock);
        handlerWithoutModule.handleCommand(floodlightChannelUID, OnOffType.ON);

        verify(requestExecutorMock, never()).executeGETRequest(any()); // no executions because the thing isn't
                                                                       // initialized
    }

    @Test
    public void testGetNAThingPropertyCommonChannel() {
        assertEquals(OnOffType.OFF, handler.getNAThingProperty(NetatmoBindingConstants.CHANNEL_CAMERA_STATUS));
    }

    @Test
    public void testGetNAThingPropertyFloodlightOn() {
        presenceCamera.setLightModeStatus(NAWelcomeCamera.LightModeStatusEnum.ON);
        assertEquals(OnOffType.ON, handler.getNAThingProperty(floodlightChannelUID.getId()));
    }

    @Test
    public void testGetNAThingPropertyFloodlightOff() {
        presenceCamera.setLightModeStatus(NAWelcomeCamera.LightModeStatusEnum.OFF);
        assertEquals(OnOffType.OFF, handler.getNAThingProperty(floodlightChannelUID.getId()));
    }

    @Test
    public void testGetNAThingPropertyFloodlightAuto() {
        presenceCamera.setLightModeStatus(NAWelcomeCamera.LightModeStatusEnum.AUTO);
        // When the floodlight is set to auto-mode it is currently off.
        assertEquals(OnOffType.OFF, handler.getNAThingProperty(floodlightChannelUID.getId()));
    }

    @Test
    public void testGetNAThingPropertyFloodlightWithoutLightModeState() {
        assertEquals(OnOffType.OFF, handler.getNAThingProperty(floodlightChannelUID.getId()));
    }

    @Test
    public void testGetNAThingPropertyFloodlightModuleNULL() {
        NAPresenceCameraHandler handlerWithoutModule = new NAPresenceCameraHandler(presenceCameraThing,
                timeZoneProviderMock);
        assertEquals(UnDefType.UNDEF, handlerWithoutModule.getNAThingProperty(floodlightChannelUID.getId()));
    }

    @Test
    public void testGetNAThingPropertyFloodlightAutoModeFloodlightAuto() {
        presenceCamera.setLightModeStatus(NAWelcomeCamera.LightModeStatusEnum.AUTO);
        assertEquals(OnOffType.ON, handler.getNAThingProperty(floodlightAutoModeChannelUID.getId()));
    }

    @Test
    public void testGetNAThingPropertyFloodlightAutoModeFloodlightOn() {
        presenceCamera.setLightModeStatus(NAWelcomeCamera.LightModeStatusEnum.ON);
        // When the floodlight is initially on (on starting the binding), there is no information about if the auto-mode
        // was set before. Therefore the auto-mode is detected as deactivated / off.
        assertEquals(OnOffType.OFF, handler.getNAThingProperty(floodlightAutoModeChannelUID.getId()));
    }

    @Test
    public void testGetNAThingPropertyFloodlightAutoModeFloodlightOff() {
        presenceCamera.setLightModeStatus(NAWelcomeCamera.LightModeStatusEnum.ON);
        // When the floodlight is initially off (on starting the binding), the auto-mode isn't set.
        assertEquals(OnOffType.OFF, handler.getNAThingProperty(floodlightAutoModeChannelUID.getId()));
    }

    @Test
    public void testGetNAThingPropertyFloodlightScenarioWithAutoMode() {
        presenceCamera.setLightModeStatus(NAWelcomeCamera.LightModeStatusEnum.AUTO);
        assertEquals(OnOffType.ON, handler.getNAThingProperty(floodlightAutoModeChannelUID.getId()));
        assertEquals(OnOffType.OFF, handler.getNAThingProperty(floodlightChannelUID.getId()));

        // The auto-mode was initially set, after that the floodlight was switched on by the user.
        // In this case the binding should still know that the auto-mode is/was set.
        presenceCamera.setLightModeStatus(NAWelcomeCamera.LightModeStatusEnum.ON);
        assertEquals(OnOffType.ON, handler.getNAThingProperty(floodlightAutoModeChannelUID.getId()));
        assertEquals(OnOffType.ON, handler.getNAThingProperty(floodlightChannelUID.getId()));

        // After that the user switched off the floodlight.
        // In this case the binding should still know that the auto-mode is/was set.
        presenceCamera.setLightModeStatus(NAWelcomeCamera.LightModeStatusEnum.OFF);
        assertEquals(OnOffType.ON, handler.getNAThingProperty(floodlightAutoModeChannelUID.getId()));
        assertEquals(OnOffType.OFF, handler.getNAThingProperty(floodlightChannelUID.getId()));
    }

    @Test
    public void testGetNAThingPropertyFloodlightScenarioWithoutAutoMode() {
        presenceCamera.setLightModeStatus(NAWelcomeCamera.LightModeStatusEnum.OFF);
        assertEquals(OnOffType.OFF, handler.getNAThingProperty(floodlightAutoModeChannelUID.getId()));
        assertEquals(OnOffType.OFF, handler.getNAThingProperty(floodlightChannelUID.getId()));

        // The auto-mode wasn't set, after that the floodlight was switched on by the user.
        // In this case the binding should still know that the auto-mode isn't/wasn't set.
        presenceCamera.setLightModeStatus(NAWelcomeCamera.LightModeStatusEnum.ON);
        assertEquals(OnOffType.OFF, handler.getNAThingProperty(floodlightAutoModeChannelUID.getId()));
        assertEquals(OnOffType.ON, handler.getNAThingProperty(floodlightChannelUID.getId()));

        // After that the user switched off the floodlight.
        // In this case the binding should still know that the auto-mode isn't/wasn't set.
        presenceCamera.setLightModeStatus(NAWelcomeCamera.LightModeStatusEnum.OFF);
        assertEquals(OnOffType.OFF, handler.getNAThingProperty(floodlightAutoModeChannelUID.getId()));
        assertEquals(OnOffType.OFF, handler.getNAThingProperty(floodlightChannelUID.getId()));
    }

    @Test
    public void testGetNAThingPropertyFloodlightAutoModeModuleNULL() {
        NAPresenceCameraHandler handlerWithoutModule = new NAPresenceCameraHandler(presenceCameraThing,
                timeZoneProviderMock);
        assertEquals(UnDefType.UNDEF, handlerWithoutModule.getNAThingProperty(floodlightAutoModeChannelUID.getId()));
    }

    @Test
    public void testGetStreamURL() {
        presenceCamera.setVpnUrl(DUMMY_VPN_URL);
        Optional<String> streamURL = handler.getStreamURL("dummyVideoId");
        assertTrue(streamURL.isPresent());
        assertEquals(DUMMY_VPN_URL + "/vod/dummyVideoId/index.m3u8", streamURL.get());
    }

    @Test
    public void testGetStreamURLLocal() {
        presenceCamera.setVpnUrl(DUMMY_VPN_URL);
        presenceCamera.setIsLocal(true);

        Optional<String> streamURL = handler.getStreamURL("dummyVideoId");
        assertTrue(streamURL.isPresent());
        assertEquals(DUMMY_VPN_URL + "/vod/dummyVideoId/index_local.m3u8", streamURL.get());
    }

    @Test
    public void testGetStreamURLNotLocal() {
        presenceCamera.setVpnUrl(DUMMY_VPN_URL);
        presenceCamera.setIsLocal(false);

        Optional<String> streamURL = handler.getStreamURL("dummyVideoId");
        assertTrue(streamURL.isPresent());
        assertEquals(DUMMY_VPN_URL + "/vod/dummyVideoId/index.m3u8", streamURL.get());
    }

    @Test
    public void testGetStreamURLWithoutVPN() {
        Optional<String> streamURL = handler.getStreamURL("dummyVideoId");
        assertFalse(streamURL.isPresent());
    }

    @Test
    public void testGetLivePictureURLState() {
        presenceCamera.setVpnUrl(DUMMY_VPN_URL);

        State livePictureURLState = handler.getLivePictureURLState();
        assertEquals(new StringType(DUMMY_VPN_URL + "/live/snapshot_720.jpg"), livePictureURLState);
    }

    @Test
    public void testGetLivePictureURLStateWithoutVPN() {
        State livePictureURLState = handler.getLivePictureURLState();
        assertEquals(UnDefType.UNDEF, livePictureURLState);
    }

    @Test
    public void testGetLiveStreamState() {
        presenceCamera.setVpnUrl(DUMMY_VPN_URL);

        State liveStreamState = handler.getLiveStreamState();
        assertEquals(new StringType(DUMMY_VPN_URL + "/live/index.m3u8"), liveStreamState);
    }

    @Test
    public void testGetLiveStreamStateWithoutVPN() {
        State liveStreamState = handler.getLiveStreamState();
        assertEquals(UnDefType.UNDEF, liveStreamState);
    }

    private static Optional<String> createPingResponseContent(final String localURL) {
        return Optional.of("{\"local_url\":\"" + localURL + "\",\"product_name\":\"Welcome Netatmo\"}");
    }

    private interface RequestExecutor {

        Optional<String> executeGETRequest(String url);
    }

    private class NAPresenceCameraHandlerAccessible extends NAPresenceCameraHandler {

        private NAPresenceCameraHandlerAccessible(Thing thing, NAWelcomeCamera presenceCamera) {
            super(thing, timeZoneProviderMock);
            setModule(presenceCamera);
        }

        @Override
        protected @NonNull Optional<@NonNull String> executeGETRequest(@NonNull String url) {
            return requestExecutorMock.executeGETRequest(url);
        }

        @Override
        protected @NonNull State getLivePictureURLState() {
            return super.getLivePictureURLState();
        }

        @Override
        protected @NonNull State getLiveStreamState() {
            return super.getLiveStreamState();
        }
    }
}
