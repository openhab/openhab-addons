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
package org.openhab.binding.unifiaccess.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifiaccess.internal.UnifiAccessBindingConstants;
import org.openhab.binding.unifiaccess.internal.api.UniFiAccessApiClient;
import org.openhab.binding.unifiaccess.internal.config.UnifiAccessDeviceConfiguration;
import org.openhab.binding.unifiaccess.internal.dto.DeviceAccessMethodSettings;
import org.openhab.binding.unifiaccess.internal.dto.DoorEmergencySettings;
import org.openhab.binding.unifiaccess.internal.dto.Notification.LocationState;
import org.openhab.binding.unifiaccess.internal.dto.Notification.RemoteViewChangeData;
import org.openhab.binding.unifiaccess.internal.dto.Notification.RemoteViewData;
import org.openhab.binding.unifiaccess.internal.dto.UniFiAccessApiException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thing handler for UniFi Access Device things.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiAccessDeviceHandler extends UnifiAccessBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(UnifiAccessDeviceHandler.class);
    private @Nullable String locationId;

    public UnifiAccessDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        deviceId = getConfigAs(UnifiAccessDeviceConfiguration.class).deviceId;
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();
        if (command instanceof RefreshType) {
            refreshState(channelId);
            return;
        }
        UnifiAccessBridgeHandler bridge = getBridgeHandler();
        UniFiAccessApiClient api = bridge != null ? bridge.getApiClient() : null;
        if (api == null) {
            return;
        }
        try {
            DeviceAccessMethodSettings current = api.getDeviceAccessMethodSettings(deviceId);
            boolean updated = false;
            if (command instanceof OnOffType onOff) {
                boolean enable = onOff == OnOffType.ON;
                switch (channelId) {
                    case UnifiAccessBindingConstants.CHANNEL_DEVICE_NFC_ENABLED:
                        if (current.nfc == null) {
                            current.nfc = new DeviceAccessMethodSettings.Nfc();
                        }
                        current.nfc.enabled = enable;
                        updated = true;
                        break;
                    case UnifiAccessBindingConstants.CHANNEL_DEVICE_PIN_ENABLED:
                        if (current.pinCode == null) {
                            current.pinCode = new DeviceAccessMethodSettings.PinCode();
                        }
                        current.pinCode.enabled = enable;
                        updated = true;
                        break;
                    case UnifiAccessBindingConstants.CHANNEL_DEVICE_PIN_SHUFFLE:
                        if (current.pinCode == null) {
                            current.pinCode = new DeviceAccessMethodSettings.PinCode();
                        }
                        current.pinCode.pinCodeShuffle = enable;
                        updated = true;
                        break;
                    case UnifiAccessBindingConstants.CHANNEL_DEVICE_FACE_ENABLED:
                        if (current.face == null) {
                            current.face = new DeviceAccessMethodSettings.Face();
                        }
                        current.face.enabled = enable;
                        updated = true;
                        break;
                    case UnifiAccessBindingConstants.CHANNEL_DEVICE_MOBILE_TAP_ENABLED:
                        if (current.btTap == null) {
                            current.btTap = new DeviceAccessMethodSettings.Bt();
                        }
                        current.btTap.enabled = enable;
                        updated = true;
                        break;
                    case UnifiAccessBindingConstants.CHANNEL_DEVICE_MOBILE_BUTTON_ENABLED:
                        if (current.btButton == null) {
                            current.btButton = new DeviceAccessMethodSettings.Bt();
                        }
                        current.btButton.enabled = enable;
                        updated = true;
                        break;
                    case UnifiAccessBindingConstants.CHANNEL_DEVICE_MOBILE_SHAKE_ENABLED:
                        if (current.btShake == null) {
                            current.btShake = new DeviceAccessMethodSettings.Bt();
                        }
                        current.btShake.enabled = enable;
                        updated = true;
                        break;
                    case UnifiAccessBindingConstants.CHANNEL_DEVICE_MOBILE_WAVE_ENABLED:
                        if (current.mobileWave == null) {
                            current.mobileWave = new DeviceAccessMethodSettings.MobileWave();
                        }
                        current.mobileWave.enabled = enable;
                        updated = true;
                        break;
                    case UnifiAccessBindingConstants.CHANNEL_DEVICE_WAVE_ENABLED:
                        if (current.wave == null) {
                            current.wave = new DeviceAccessMethodSettings.Wave();
                        }
                        current.wave.enabled = enable;
                        updated = true;
                        break;
                    case UnifiAccessBindingConstants.CHANNEL_DEVICE_QR_CODE_ENABLED:
                        if (current.qrCode == null) {
                            current.qrCode = new DeviceAccessMethodSettings.QrCode();
                        }
                        current.qrCode.enabled = enable;
                        updated = true;
                        break;
                    case UnifiAccessBindingConstants.CHANNEL_DEVICE_TOUCH_PASS_ENABLED:
                        if (current.touchPass == null) {
                            current.touchPass = new DeviceAccessMethodSettings.TouchPass();
                        }
                        current.touchPass.enabled = enable;
                        updated = true;
                        break;
                    default:
                        break;
                }
            } else {
                String value = command.toString();
                switch (channelId) {
                    case UnifiAccessBindingConstants.CHANNEL_DEVICE_FACE_ANTI_SPOOFING:
                        if (current.face == null) {
                            current.face = new DeviceAccessMethodSettings.Face();
                        }
                        current.face.antiSpoofingLevel = value; // expects high|medium|no
                        updated = true;
                        break;
                    case UnifiAccessBindingConstants.CHANNEL_DEVICE_FACE_DETECT_DISTANCE:
                        if (current.face == null) {
                            current.face = new DeviceAccessMethodSettings.Face();
                        }
                        current.face.detectDistance = value; // expects near|medium|far
                        updated = true;
                        break;
                    case UnifiAccessBindingConstants.CHANNEL_DEVICE_EMERGENCY_STATUS:
                        String normalized = value.toLowerCase();
                        DoorEmergencySettings des = new DoorEmergencySettings();
                        String status = "normal";
                        if ("lockdown".equals(normalized)) {
                            des.lockdown = Boolean.TRUE;
                            des.evacuation = Boolean.FALSE;
                            status = "lockdown";
                        } else if ("evacuation".equals(normalized)) {
                            des.lockdown = Boolean.FALSE;
                            des.evacuation = Boolean.TRUE;
                            status = "evacuation";
                        } else {
                            des.lockdown = Boolean.FALSE;
                            des.evacuation = Boolean.FALSE;
                            status = "normal";
                        }
                        try {
                            String doorId = this.locationId;
                            if (doorId != null && !doorId.isBlank()) {
                                api.setDoorEmergencySettings(doorId, des);
                                updateState(UnifiAccessBindingConstants.CHANNEL_DEVICE_EMERGENCY_STATUS,
                                        new StringType(status));
                            }
                        } catch (UniFiAccessApiException e) {
                            logger.debug("Failed to set door emergency settings for device {}: {}", deviceId,
                                    e.getMessage());
                            updateState(UnifiAccessBindingConstants.CHANNEL_DEVICE_EMERGENCY_STATUS, UnDefType.UNDEF);
                        }
                        break;
                    default:
                        break;
                }
            }

            if (updated) {
                DeviceAccessMethodSettings saved = api.updateDeviceAccessMethodSettings(deviceId, current);
                updateFromSettings(saved);
            }
        } catch (Exception e) {
            logger.debug("Command failed for device {}: {}", deviceId, e.getMessage());
        }
    }

    @Override
    protected void handleLocationState(LocationState locationState) {
        this.locationId = locationState.locationId;
        if (locationState.dps != null) {
            updateState(UnifiAccessBindingConstants.CHANNEL_DEVICE_DOOR_SENSOR,
                    locationState.dps == org.openhab.binding.unifiaccess.internal.dto.DoorState.DoorPosition.OPEN
                            ? OpenClosedType.OPEN
                            : OpenClosedType.CLOSED);
        }
        String status = "normal";
        if (locationState.emergency != null) {
            String sw = locationState.emergency.software;
            String hw = locationState.emergency.hardware;
            if ("lockdown".equalsIgnoreCase(sw) || "lockdown".equalsIgnoreCase(hw)) {
                status = "lockdown";
            } else if ("evacuation".equalsIgnoreCase(sw) || "evacuation".equalsIgnoreCase(hw)) {
                status = "evacuation";
            }
        }
        updateState(UnifiAccessBindingConstants.CHANNEL_DEVICE_EMERGENCY_STATUS, new StringType(status));
    }

    protected void updateFromSettings(DeviceAccessMethodSettings settings) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
        if (settings.nfc != null && settings.nfc.enabled != null) {
            updateState(UnifiAccessBindingConstants.CHANNEL_DEVICE_NFC_ENABLED,
                    settings.nfc.enabled ? OnOffType.ON : OnOffType.OFF);
        }
        if (settings.pinCode != null) {
            if (settings.pinCode.enabled != null) {
                updateState(UnifiAccessBindingConstants.CHANNEL_DEVICE_PIN_ENABLED,
                        settings.pinCode.enabled ? OnOffType.ON : OnOffType.OFF);
            }
            if (settings.pinCode.pinCodeShuffle != null) {
                updateState(UnifiAccessBindingConstants.CHANNEL_DEVICE_PIN_SHUFFLE,
                        settings.pinCode.pinCodeShuffle ? OnOffType.ON : OnOffType.OFF);
            }
        }
        if (settings.btTap != null && settings.btTap.enabled != null) {
            updateState(UnifiAccessBindingConstants.CHANNEL_DEVICE_MOBILE_TAP_ENABLED,
                    settings.btTap.enabled ? OnOffType.ON : OnOffType.OFF);
        }
        if (settings.btButton != null && settings.btButton.enabled != null) {
            updateState(UnifiAccessBindingConstants.CHANNEL_DEVICE_MOBILE_BUTTON_ENABLED,
                    settings.btButton.enabled ? OnOffType.ON : OnOffType.OFF);
        }
        if (settings.btShake != null && settings.btShake.enabled != null) {
            updateState(UnifiAccessBindingConstants.CHANNEL_DEVICE_MOBILE_SHAKE_ENABLED,
                    settings.btShake.enabled ? OnOffType.ON : OnOffType.OFF);
        }
        if (settings.mobileWave != null && settings.mobileWave.enabled != null) {
            updateState(UnifiAccessBindingConstants.CHANNEL_DEVICE_MOBILE_WAVE_ENABLED,
                    settings.mobileWave.enabled ? OnOffType.ON : OnOffType.OFF);
        }
        if (settings.wave != null && settings.wave.enabled != null) {
            updateState(UnifiAccessBindingConstants.CHANNEL_DEVICE_WAVE_ENABLED,
                    settings.wave.enabled ? OnOffType.ON : OnOffType.OFF);
        }
        if (settings.qrCode != null && settings.qrCode.enabled != null) {
            updateState(UnifiAccessBindingConstants.CHANNEL_DEVICE_QR_CODE_ENABLED,
                    settings.qrCode.enabled ? OnOffType.ON : OnOffType.OFF);
        }
        if (settings.touchPass != null && settings.touchPass.enabled != null) {
            updateState(UnifiAccessBindingConstants.CHANNEL_DEVICE_TOUCH_PASS_ENABLED,
                    settings.touchPass.enabled ? OnOffType.ON : OnOffType.OFF);
        }
        if (settings.face != null && settings.face.enabled != null) {
            updateState(UnifiAccessBindingConstants.CHANNEL_DEVICE_FACE_ENABLED,
                    settings.face.enabled ? OnOffType.ON : OnOffType.OFF);
        }
        if (settings.face != null && settings.face.antiSpoofingLevel != null) {
            updateState(UnifiAccessBindingConstants.CHANNEL_DEVICE_FACE_ANTI_SPOOFING,
                    new StringType(settings.face.antiSpoofingLevel));
        }
        if (settings.face != null && settings.face.detectDistance != null) {
            updateState(UnifiAccessBindingConstants.CHANNEL_DEVICE_FACE_DETECT_DISTANCE,
                    new StringType(settings.face.detectDistance));
        }
    }

    protected void handleRemoteView(RemoteViewData remoteView) {
        if (!deviceId.equals(remoteView.deviceId)) {
            return;
        }
        triggerChannel(UnifiAccessBindingConstants.CHANNEL_DEVICE_DOORBELL_TRIGGER, "incoming");
        updateState(UnifiAccessBindingConstants.CHANNEL_DEVICE_DOORBELL_CONTACT, OpenClosedType.OPEN);
    }

    protected void handleRemoteViewChange(RemoteViewChangeData change) {
        triggerChannel(UnifiAccessBindingConstants.CHANNEL_DEVICE_DOORBELL_TRIGGER, "completed");
        updateState(UnifiAccessBindingConstants.CHANNEL_DEVICE_DOORBELL_CONTACT, OpenClosedType.CLOSED);
        String event = change.reason != null ? change.reason.name() : "UNKNOWN";
        triggerChannel(UnifiAccessBindingConstants.CHANNEL_DOORBELL_STATUS, event);
    }

    protected void triggerLogInsight(String payload) {
        triggerChannel(UnifiAccessBindingConstants.CHANNEL_BRIDGE_LOG_INSIGHT, payload);
    }
}
