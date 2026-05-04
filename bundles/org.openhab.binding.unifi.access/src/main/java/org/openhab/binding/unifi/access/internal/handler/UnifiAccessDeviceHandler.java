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
package org.openhab.binding.unifi.access.internal.handler;

import static org.openhab.binding.unifi.access.internal.UnifiAccessBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.access.internal.api.UnifiAccessApiClient;
import org.openhab.binding.unifi.access.internal.config.UnifiAccessDeviceConfiguration;
import org.openhab.binding.unifi.access.internal.dto.Device;
import org.openhab.binding.unifi.access.internal.dto.DeviceAccessMethodSettings;
import org.openhab.binding.unifi.access.internal.dto.DeviceAccessMethodSettings.EnabledFlag;
import org.openhab.binding.unifi.access.internal.dto.Door;
import org.openhab.binding.unifi.access.internal.dto.DoorState;
import org.openhab.binding.unifi.access.internal.dto.Notification.LocationState;
import org.openhab.binding.unifi.access.internal.dto.Notification.RemoteViewChangeData;
import org.openhab.binding.unifi.access.internal.dto.Notification.RemoteViewData;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
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

    private static final Map<String, String> CHANNEL_TO_CONFIG_KEY = Map.ofEntries(
            Map.entry(CHANNEL_DEVICE_NFC_ENABLED, "nfc"), Map.entry(CHANNEL_DEVICE_PIN_ENABLED, "pin_code"),
            Map.entry(CHANNEL_DEVICE_PIN_SHUFFLE, "pin_code_shuffle"), Map.entry(CHANNEL_DEVICE_FACE_ENABLED, "face"),
            Map.entry(CHANNEL_DEVICE_MOBILE_TAP_ENABLED, "bt_tap"),
            Map.entry(CHANNEL_DEVICE_MOBILE_BUTTON_ENABLED, "bt_button"),
            Map.entry(CHANNEL_DEVICE_MOBILE_SHAKE_ENABLED, "bt"),
            Map.entry(CHANNEL_DEVICE_MOBILE_WAVE_ENABLED, "camera_mobile_unlock"),
            Map.entry(CHANNEL_DEVICE_WAVE_ENABLED, "wave"), Map.entry(CHANNEL_DEVICE_QR_CODE_ENABLED, "qr_code"),
            Map.entry(CHANNEL_DEVICE_TOUCH_PASS_ENABLED, "apple_pass"),
            Map.entry(CHANNEL_DEVICE_FACE_ANTI_SPOOFING, "face_anti_spoofing_level"),
            Map.entry(CHANNEL_DEVICE_FACE_DETECT_DISTANCE, "face_detect_distance_v2"));

    /** Cached config map from the last bootstrap sync, used for building settings on command. */
    private Map<String, String> lastConfigMap = new ConcurrentHashMap<>();

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
        UnifiAccessApiClient api = bridge != null ? bridge.getApiClient() : null;
        if (api == null) {
            return;
        }
        // Map channel commands to config key/value for the v2 configs API
        String configKey = channelToConfigKey(channelId);
        if (configKey == null) {
            return;
        }
        String configValue;
        if (command instanceof OnOffType onOff) {
            configValue = onOff == OnOffType.ON ? "yes" : "no";
        } else {
            configValue = command.toString();
        }

        // Save previous state for rollback on failure
        State previousState = stateCache.get(channelId);
        try {
            api.updateDeviceConfig(deviceId, configKey, configValue);
            // Update local configMap and channel
            lastConfigMap.put(configKey, configValue);
            var settings = UnifiAccessApiClient.buildSettingsFromConfigs(lastConfigMap);
            updateFromSettings(settings);
        } catch (Exception e) {
            logger.debug("Command failed for device {}: {}", deviceId, e.getMessage());
            // Revert channel state on failure
            if (previousState != null) {
                updateState(channelId, previousState);
            }
        }
    }

    @Override
    protected void handleLocationState(LocationState locationState) {
        if (locationState.dps != null) {
            updateState(CHANNEL_DEVICE_DOOR_SENSOR,
                    locationState.dps == DoorState.DoorPosition.OPEN ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        }
    }

    protected void updateFromSettings(DeviceAccessMethodSettings settings) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
        updateEnabledChannel(CHANNEL_DEVICE_NFC_ENABLED, settings.nfc);
        updateEnabledChannel(CHANNEL_DEVICE_PIN_ENABLED, settings.pinCode);
        updateEnabledChannel(CHANNEL_DEVICE_FACE_ENABLED, settings.face);
        updateEnabledChannel(CHANNEL_DEVICE_MOBILE_TAP_ENABLED, settings.btTap);
        updateEnabledChannel(CHANNEL_DEVICE_MOBILE_BUTTON_ENABLED, settings.btButton);
        updateEnabledChannel(CHANNEL_DEVICE_MOBILE_SHAKE_ENABLED, settings.btShake);
        updateEnabledChannel(CHANNEL_DEVICE_MOBILE_WAVE_ENABLED, settings.mobileWave);
        updateEnabledChannel(CHANNEL_DEVICE_WAVE_ENABLED, settings.wave);
        updateEnabledChannel(CHANNEL_DEVICE_QR_CODE_ENABLED, settings.qrCode);
        updateEnabledChannel(CHANNEL_DEVICE_TOUCH_PASS_ENABLED, settings.touchPass);

        if (settings.pinCode != null) {
            updateState(CHANNEL_DEVICE_PIN_SHUFFLE, settings.pinCode.isShuffleEnabled() ? OnOffType.ON : OnOffType.OFF);
        }
        if (settings.face != null) {
            if (settings.face.antiSpoofingLevel != null) {
                updateState(CHANNEL_DEVICE_FACE_ANTI_SPOOFING, new StringType(settings.face.antiSpoofingLevel));
            }
            if (settings.face.detectDistance != null) {
                updateState(CHANNEL_DEVICE_FACE_DETECT_DISTANCE, new StringType(settings.face.detectDistance));
            }
        }
    }

    protected void handleRemoteView(RemoteViewData remoteView) {
        if (!deviceId.equals(remoteView.deviceId)) {
            return;
        }
        // Distinguish REN (Request-to-Enter) from regular doorbell
        String event = remoteView.isRen() ? "incoming-ren" : "incoming";
        triggerChannel(CHANNEL_DEVICE_DOORBELL_TRIGGER, event);
        updateState(CHANNEL_DEVICE_DOORBELL_CONTACT, OpenClosedType.OPEN);
    }

    protected void handleRemoteViewChange(RemoteViewChangeData change) {
        triggerChannel(CHANNEL_DEVICE_DOORBELL_TRIGGER, "completed");
        updateState(CHANNEL_DEVICE_DOORBELL_CONTACT, OpenClosedType.CLOSED);
        String event = change.reason != null ? change.reason.name() : "UNKNOWN";
        triggerChannel(CHANNEL_DOORBELL_STATUS, event);
    }

    protected void triggerLogInsight(String payload) {
        triggerChannel(CHANNEL_BRIDGE_LOG_INSIGHT, payload);
    }

    /**
     * Stores the device config map from the bootstrap for use during command handling.
     */
    void updateConfigMap(Map<String, String> configMap) {
        this.lastConfigMap = new HashMap<>(configMap);
    }

    /**
     * Cross-populates door state (position sensor) to this device's channels.
     * Called during sync for devices whose locationId matches a door.
     */
    void updateFromDoor(Door door) {
        logger.debug("Cross-populating door state to device {}: lock={}, position={}", deviceId,
                door.doorLockRelayStatus, door.doorPositionStatus);
        if (door.doorPositionStatus != null) {
            updateState(CHANNEL_DEVICE_DOOR_SENSOR,
                    door.doorPositionStatus == DoorState.DoorPosition.OPEN ? OpenClosedType.OPEN
                            : OpenClosedType.CLOSED);
        }
    }

    /**
     * Handles a hardware doorbell button press event.
     */
    protected void handleHwDoorbell() {
        triggerChannel(CHANNEL_DEVICE_DOORBELL_TRIGGER, "pressed");
    }

    /**
     * Updates thing properties from device metadata.
     */
    void updateDeviceProperties(Device device) {
        Map<String, String> properties = new HashMap<>(editProperties());
        String type = device.type;
        if (type != null) {
            properties.put("deviceType", type);
        }
        String alias = device.alias;
        if (alias != null) {
            properties.put("alias", alias);
        }
        String name = device.name;
        if (name != null) {
            properties.put("deviceName", name);
        }
        String mac = device.mac;
        if (mac != null) {
            properties.put("macAddress", mac);
        }
        String firmware = device.firmware;
        if (firmware != null) {
            properties.put("firmware", firmware);
        }
        String model = device.displayModel;
        if (model != null) {
            properties.put("model", model);
        }
        updateProperties(properties);
    }

    private static @Nullable String channelToConfigKey(String channelId) {
        return CHANNEL_TO_CONFIG_KEY.get(channelId);
    }

    private void updateEnabledChannel(String channelId, @Nullable EnabledFlag flag) {
        if (flag != null) {
            updateState(channelId, flag.isEnabled() ? OnOffType.ON : OnOffType.OFF);
        }
    }
}
