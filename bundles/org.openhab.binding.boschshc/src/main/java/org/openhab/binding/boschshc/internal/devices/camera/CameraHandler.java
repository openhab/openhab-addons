/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_CAMERA_NOTIFICATION;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_PRIVACY_MODE;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.BoschSHCDeviceHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.cameranotification.CameraNotificationService;
import org.openhab.binding.boschshc.internal.services.cameranotification.dto.CameraNotificationServiceState;
import org.openhab.binding.boschshc.internal.services.dto.EnabledDisabledState;
import org.openhab.binding.boschshc.internal.services.privacymode.PrivacyModeService;
import org.openhab.binding.boschshc.internal.services.privacymode.dto.PrivacyModeServiceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Handler for security cameras.
 * <p>
 * This implementation handles services and commands that are common to all cameras, which are currently:
 *
 * <ul>
 * <li><code>PrivacyMode</code> - Controls whether the camera records images</li>
 * <li><code>CameraNotification</code> - Enables or disables notifications for the camera</li>
 * </ul>
 *
 * <p>
 * The Eyes outdoor camera advertises a <code>CameraLight</code> service, which unfortunately does not work properly.
 * Valid states are <code>ON</code> and <code>OFF</code>.
 * One of my two cameras returns <code>HTTP 204 (No Content)</code> when requesting the state.
 * Once Bosch supports this service properly, a new subclass may be introduced for the Eyes outdoor camera.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class CameraHandler extends BoschSHCDeviceHandler {

    private PrivacyModeService privacyModeService;
    private CameraNotificationService cameraNotificationService;

    public CameraHandler(Thing thing) {
        super(thing);
        this.privacyModeService = new PrivacyModeService();
        this.cameraNotificationService = new CameraNotificationService();
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        this.registerService(this.privacyModeService, this::updateChannels, List.of(CHANNEL_PRIVACY_MODE), true);
        this.registerService(this.cameraNotificationService, this::updateChannels, List.of(CHANNEL_CAMERA_NOTIFICATION),
                true);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        switch (channelUID.getId()) {
            case CHANNEL_PRIVACY_MODE:
                if (command instanceof OnOffType onOffCommand) {
                    updatePrivacyModeState(onOffCommand);
                }
                break;

            case CHANNEL_CAMERA_NOTIFICATION:
                if (command instanceof OnOffType onOffCommand) {
                    updateCameraNotificationState(onOffCommand);
                }
                break;
        }
    }

    private void updatePrivacyModeState(OnOffType command) {
        PrivacyModeServiceState serviceState = new PrivacyModeServiceState();
        serviceState.value = EnabledDisabledState.from(command);
        this.updateServiceState(this.privacyModeService, serviceState);
    }

    private void updateCameraNotificationState(OnOffType command) {
        CameraNotificationServiceState serviceState = new CameraNotificationServiceState();
        serviceState.value = EnabledDisabledState.from(command);
        this.updateServiceState(this.cameraNotificationService, serviceState);
    }

    private void updateChannels(PrivacyModeServiceState state) {
        super.updateState(CHANNEL_PRIVACY_MODE, state.value.toOnOffType());
    }

    private void updateChannels(CameraNotificationServiceState state) {
        super.updateState(CHANNEL_CAMERA_NOTIFICATION, state.value.toOnOffType());
    }
}
