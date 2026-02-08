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
package org.openhab.binding.ring;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RingBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */
@NonNullByDefault
public class RingBindingConstants {

    public static final String BINDING_ID = "ring";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_DOORBELL = new ThingTypeUID(BINDING_ID, "doorbell");
    public static final ThingTypeUID THING_TYPE_CHIME = new ThingTypeUID(BINDING_ID, "chime");
    public static final ThingTypeUID THING_TYPE_STICKUPCAM = new ThingTypeUID(BINDING_ID, "stickupcam");
    public static final ThingTypeUID THING_TYPE_OTHERDEVICE = new ThingTypeUID(BINDING_ID, "otherdevice");

    // List of all Channel ids
    public static final String CHANNEL_CONTROL_STATUS = "control#status";
    public static final String CHANNEL_CONTROL_ENABLED = "control#enabled";

    public static final String CHANNEL_STATUS_BATTERY = "status#battery";

    public static final String CHANNEL_STATUS_SNAPSHOT = "status#snapshot";
    public static final String CHANNEL_STATUS_SNAPSHOT_TIMESTAMP = "status#snapshot-timestamp";

    public static final String CHANNEL_EVENT_URL = "event#url";
    public static final String CHANNEL_EVENT_CREATED_AT = "event#createdAt";
    public static final String CHANNEL_EVENT_KIND = "event#kind";
    public static final String CHANNEL_EVENT_DOORBOT_ID = "event#doorbotId";
    public static final String CHANNEL_EVENT_DOORBOT_DESCRIPTION = "event#doorbotDescription";

    public static final String SERVLET_VIDEO_PATH = "/ring/video";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_DOORBELL,
            THING_TYPE_CHIME, THING_TYPE_STICKUPCAM, THING_TYPE_OTHERDEVICE);

    public static final String THING_CONFIG_ID = "id";
    public static final String THING_PROPERTY_DESCRIPTION = "description";
    public static final String THING_PROPERTY_KIND = "kind";
    public static final String THING_PROPERTY_DEVICE_ID = "deviceId";
    public static final String THING_PROPERTY_OWNER_ID = "ownerId";

    // device model kinds
    public static final Set<String> CHIME_KINDS = Set.of("chime", "chime_v2");
    public static final Set<String> CHIME_PRO_KINDS = Set.of("chime_pro", "chime_pro_v2");

    public static final Set<String> DOORBELL_KINDS = Set.of("doorbot", "doorbell", "doorbell_v3");
    public static final Set<String> DOORBELL_2_KINDS = Set.of("doorbell_v4", "doorbell_v5");
    public static final Set<String> DOORBELL_3_KINDS = Set.of("doorbell_scallop_lite");
    public static final Set<String> DOORBELL_4_KINDS = Set.of("doorbell_oyster");
    public static final Set<String> DOORBELL_3_PLUS_KINDS = Set.of("doorbell_scallop");
    public static final Set<String> DOORBELL_PRO_KINDS = Set.of("lpd_v1", "lpd_v2", "lpd_v3");
    public static final Set<String> DOORBELL_PRO_2_KINDS = Set.of("lpd_v4");
    public static final Set<String> DOORBELL_ELITE_KINDS = Set.of("jbox_v1");
    public static final Set<String> DOORBELL_WIRED_KINDS = Set.of("doorbell_graham_cracker");
    public static final Set<String> DOORBELL_BATTERY_KINDS = Set.of("df_doorbell_clownfish");
    public static final Set<String> PEEPHOLE_CAM_KINDS = Set.of("doorbell_portal");
    public static final Set<String> DOORBELL_GEN2_KINDS = Set.of("cocoa_doorbell", "cocoa_doorbell_v2");

    public static final Set<String> FLOODLIGHT_CAM_KINDS = Set.of("hp_cam_v1", "floodlight_v2");
    public static final Set<String> FLOODLIGHT_CAM_PRO_KINDS = Set.of("floodlight_pro");
    public static final Set<String> FLOODLIGHT_CAM_PLUS_KINDS = Set.of("cocoa_floodlight");
    public static final Set<String> INDOOR_CAM_KINDS = Set.of("stickup_cam_mini");
    public static final Set<String> INDOOR_CAM_GEN2_KINDS = Set.of("stickup_cam_mini_v2");
    public static final Set<String> INDOOR_CAM_PTZ_KINDS = Set.of("stickup_cam_mini_ptz_v1");
    public static final Set<String> SPOTLIGHT_CAM_BATTERY_KINDS = Set.of("stickup_cam_v4");
    public static final Set<String> SPOTLIGHT_CAM_WIRED_KINDS = Set.of("hp_cam_v2", "spotlightw_v2");
    public static final Set<String> SPOTLIGHT_CAM_PLUS_KINDS = Set.of("cocoa_spotlight");
    public static final Set<String> SPOTLIGHT_CAM_PRO_KINDS = Set.of("stickup_cam_longfin");
    public static final Set<String> STICKUP_CAM_KINDS = Set.of("stickup_cam", "stickup_cam_v3");
    public static final Set<String> STICKUP_CAM_BATTERY_KINDS = Set.of("stickup_cam_lunar");
    public static final Set<String> STICKUP_CAM_ELITE_KINDS = Set.of("stickup_cam_elite", "stickup_cam_wired");
    public static final Set<String> STICKUP_CAM_GEN3_KINDS = Set.of("cocoa_camera");
    public static final Set<String> BEAM_KINDS = Set.of("beams_ct200_transformer");

    public static final Set<String> INTERCOM_KINDS = Set.of("intercom_handset_audio", "intercom_handset_video");

    // battery kinds
    public static final Set<String> BATTERY_KINDS = Stream
            .of(SPOTLIGHT_CAM_BATTERY_KINDS, STICKUP_CAM_KINDS, STICKUP_CAM_BATTERY_KINDS, STICKUP_CAM_GEN3_KINDS)
            .flatMap(Set::stream).collect(Collectors.toUnmodifiableSet());

    // light kinds
    public static final Set<String> LIGHT_KINDS = Stream
            .of(FLOODLIGHT_CAM_KINDS, FLOODLIGHT_CAM_PRO_KINDS, FLOODLIGHT_CAM_PLUS_KINDS, SPOTLIGHT_CAM_BATTERY_KINDS,
                    SPOTLIGHT_CAM_WIRED_KINDS, SPOTLIGHT_CAM_PLUS_KINDS, SPOTLIGHT_CAM_PRO_KINDS)
            .flatMap(Set::stream).collect(Collectors.toUnmodifiableSet());

    // siren kinds
    public static final Set<String> SIREN_KINDS = Stream
            .of(FLOODLIGHT_CAM_KINDS, FLOODLIGHT_CAM_PRO_KINDS, FLOODLIGHT_CAM_PLUS_KINDS, INDOOR_CAM_KINDS,
                    INDOOR_CAM_GEN2_KINDS, INDOOR_CAM_PTZ_KINDS, SPOTLIGHT_CAM_BATTERY_KINDS, SPOTLIGHT_CAM_WIRED_KINDS,
                    SPOTLIGHT_CAM_PLUS_KINDS, SPOTLIGHT_CAM_PRO_KINDS, STICKUP_CAM_BATTERY_KINDS,
                    STICKUP_CAM_ELITE_KINDS, STICKUP_CAM_GEN3_KINDS)
            .flatMap(Set::stream).collect(Collectors.toUnmodifiableSet());
}
