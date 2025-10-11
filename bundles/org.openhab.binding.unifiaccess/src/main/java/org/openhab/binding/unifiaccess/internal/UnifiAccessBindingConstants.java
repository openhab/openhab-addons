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
package org.openhab.binding.unifiaccess.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link UnifiAccessBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiAccessBindingConstants {

    public static final String BINDING_ID = "unifiaccess";

    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID DOOR_THING_TYPE = new ThingTypeUID(BINDING_ID, "door");
    public static final ThingTypeUID DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, "device");

    public static final String CONFIG_DEVICE_ID = "deviceId";

    public static final String CHANNEL_LOCK = "lock";
    public static final String CHANNEL_DOOR_POSITION = "position";
    public static final String CHANNEL_LAST_UNLOCK = "last-unlock";
    public static final String CHANNEL_LAST_ACTOR = "last-actor";
    public static final String CHANNEL_LOCK_RULE = "lock-rule";

    public static final String CHANNEL_KEEP_UNLOCKED = "keep-unlocked";
    public static final String CHANNEL_KEEP_LOCKED = "keep-locked";
    public static final String CHANNEL_UNLOCK_MINUTES = "unlock-minutes";
    public static final String CHANNEL_DOOR_THUMBNAIL = "thumbnail";

    // Door trigger channels
    public static final String CHANNEL_DOOR_ACCESS_ATTEMPT_SUCCESS = "access-attempt-success";
    public static final String CHANNEL_DOOR_ACCESS_ATTEMPT_FAILURE = "access-attempt-failure";
    public static final String CHANNEL_DOOR_REMOTE_UNLOCK = "remote-unlock";
    public static final String CHANNEL_DOORBELL_STATUS = "doorbell-status";

    // Device channels
    public static final String CHANNEL_DEVICE_NFC_ENABLED = "nfc-enabled";
    public static final String CHANNEL_DEVICE_PIN_ENABLED = "pin-enabled";
    public static final String CHANNEL_DEVICE_PIN_SHUFFLE = "pin-shuffle";
    public static final String CHANNEL_DEVICE_FACE_ENABLED = "face-enabled";
    public static final String CHANNEL_DEVICE_MOBILE_TAP_ENABLED = "mobile-tap-enabled";
    public static final String CHANNEL_DEVICE_MOBILE_BUTTON_ENABLED = "mobile-button-enabled";
    public static final String CHANNEL_DEVICE_MOBILE_SHAKE_ENABLED = "mobile-shake-enabled";
    public static final String CHANNEL_DEVICE_MOBILE_WAVE_ENABLED = "mobile-wave-enabled";
    public static final String CHANNEL_DEVICE_WAVE_ENABLED = "wave-enabled";
    public static final String CHANNEL_DEVICE_EMERGENCY_STATUS = "emergency-status";
    public static final String CHANNEL_DEVICE_DOOR_SENSOR = "door-sensor";
    public static final String CHANNEL_DEVICE_DOORBELL_CONTACT = "doorbell-contact";
    public static final String CHANNEL_DEVICE_DOORBELL_TRIGGER = "doorbell";
    public static final String CHANNEL_DEVICE_QR_CODE_ENABLED = "qr-code-enabled";
    public static final String CHANNEL_DEVICE_TOUCH_PASS_ENABLED = "touch-pass-enabled";
    public static final String CHANNEL_DEVICE_FACE_ANTI_SPOOFING = "face-anti-spoofing";
    public static final String CHANNEL_DEVICE_FACE_DETECT_DISTANCE = "face-detect-distance";

    // Bridge trigger channels
    public static final String CHANNEL_BRIDGE_LOG_INSIGHT = "log-insight";
    public static final String CHANNEL_BRIDGE_LOG = "log";
}
