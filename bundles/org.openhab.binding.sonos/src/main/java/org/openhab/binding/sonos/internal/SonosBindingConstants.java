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
package org.openhab.binding.sonos.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SonosBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Karel Goderis - Initial contribution
 * @author Kai Kreuzer - Changed ESH-PREFIX and cleaned up warnings
 */
@NonNullByDefault
public class SonosBindingConstants {

    public static final String BINDING_ID = "sonos";
    public static final String TITLE_PREFIX = "smarthome-";

    // List of all Thing Type UIDs
    // Column (:) is not used for PLAY:1, PLAY:3, PLAY:5 and CONNECT:AMP because of
    // ThingTypeUID and device pairing name restrictions
    public static final ThingTypeUID ONE_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "One");
    public static final ThingTypeUID ONE_SL_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "OneSL");
    public static final ThingTypeUID PLAY1_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "PLAY1");
    public static final ThingTypeUID PLAY3_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "PLAY3");
    public static final ThingTypeUID PLAY5_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "PLAY5");
    public static final ThingTypeUID FIVE_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "Five");
    public static final ThingTypeUID PLAYBAR_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "PLAYBAR");
    public static final ThingTypeUID PLAYBASE_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "PLAYBASE");
    public static final ThingTypeUID BEAM_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "Beam");
    public static final ThingTypeUID CONNECT_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "CONNECT");
    public static final ThingTypeUID PORT_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "Port");
    public static final ThingTypeUID CONNECTAMP_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "CONNECTAMP");
    public static final ThingTypeUID AMP_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "Amp");
    public static final ThingTypeUID SYMFONISK_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "SYMFONISK");
    public static final ThingTypeUID ARC_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "Arc");
    public static final ThingTypeUID ARC_SL_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "ArcSL");
    public static final ThingTypeUID MOVE_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "Move");
    public static final ThingTypeUID MOVE2_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "Move2");
    public static final ThingTypeUID ROAM_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "Roam");
    public static final ThingTypeUID ROAM_SL_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "RoamSL");
    public static final ThingTypeUID ERA_100_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "Era100");
    public static final ThingTypeUID ERA_300_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "Era300");
    public static final ThingTypeUID ZONEPLAYER_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "zoneplayer");

    public static final Set<ThingTypeUID> WITH_LINEIN_THING_TYPES_UIDS = Set.of(PLAY5_THING_TYPE_UID,
            FIVE_THING_TYPE_UID, PLAYBAR_THING_TYPE_UID, PLAYBASE_THING_TYPE_UID, BEAM_THING_TYPE_UID,
            CONNECT_THING_TYPE_UID, CONNECTAMP_THING_TYPE_UID, PORT_THING_TYPE_UID, ARC_THING_TYPE_UID,
            ARC_SL_THING_TYPE_UID, MOVE2_THING_TYPE_UID, ERA_100_THING_TYPE_UID, ERA_300_THING_TYPE_UID);

    public static final Set<ThingTypeUID> WITH_ANALOG_LINEIN_THING_TYPES_UIDS = Set.of(AMP_THING_TYPE_UID);

    public static final Set<ThingTypeUID> WITH_DIGITAL_LINEIN_THING_TYPES_UIDS = Set.of(AMP_THING_TYPE_UID);

    public static final Set<ThingTypeUID> SUPPORTED_KNOWN_THING_TYPES_UIDS = Set.of(ONE_THING_TYPE_UID,
            ONE_SL_THING_TYPE_UID, PLAY1_THING_TYPE_UID, PLAY3_THING_TYPE_UID, PLAY5_THING_TYPE_UID,
            FIVE_THING_TYPE_UID, PLAYBAR_THING_TYPE_UID, PLAYBASE_THING_TYPE_UID, BEAM_THING_TYPE_UID,
            CONNECT_THING_TYPE_UID, CONNECTAMP_THING_TYPE_UID, PORT_THING_TYPE_UID, AMP_THING_TYPE_UID,
            SYMFONISK_THING_TYPE_UID, ARC_THING_TYPE_UID, ARC_SL_THING_TYPE_UID, MOVE_THING_TYPE_UID,
            MOVE2_THING_TYPE_UID, ROAM_THING_TYPE_UID, ROAM_SL_THING_TYPE_UID, ERA_100_THING_TYPE_UID,
            ERA_300_THING_TYPE_UID);

    public static final Set<String> UNSUPPORTED_KNOWN_IDS = Set.of("sub", "sonos sub mini");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(SUPPORTED_KNOWN_THING_TYPES_UIDS);
    static {
        SUPPORTED_THING_TYPES_UIDS.add(ZONEPLAYER_THING_TYPE_UID);
    }

    // List of all Channel ids
    public static final String ADD = "add";
    public static final String ALARM = "alarm";
    public static final String ALARMPROPERTIES = "alarmproperties";
    public static final String ALARMRUNNING = "alarmrunning";
    public static final String BASS = "bass";
    public static final String BATTERYCHARGING = "batterycharging";
    public static final String BATTERYLEVEL = "batterylevel";
    public static final String CLEARQUEUE = "clearqueue";
    public static final String CODEC = "codec";
    public static final String CONTROL = "control";
    public static final String COORDINATOR = "coordinator";
    public static final String CURRENTALBUM = "currentalbum";
    public static final String CURRENTALBUMART = "currentalbumart";
    public static final String CURRENTALBUMARTURL = "currentalbumarturl";
    public static final String CURRENTARTIST = "currentartist";
    public static final String CURRENTTITLE = "currenttitle";
    public static final String CURRENTTRACK = "currenttrack";
    public static final String CURRENTTRACKURI = "currenttrackuri";
    public static final String CURRENTTRANSPORTURI = "currenttransporturi";
    public static final String FAVORITE = "favorite";
    public static final String HEIGHTLEVEL = "heightlevel";
    public static final String LED = "led";
    public static final String LINEIN = "linein";
    public static final String ANALOGLINEIN = "analoglinein";
    public static final String DIGITALLINEIN = "digitallinein";
    public static final String LOCALCOORDINATOR = "localcoordinator";
    public static final String LOUDNESS = "loudness";
    public static final String MICROPHONE = "microphone";
    public static final String MUTE = "mute";
    public static final String NIGHTMODE = "nightmode";
    public static final String NOTIFICATIONSOUND = "notificationsound";
    public static final String PLAYLINEIN = "playlinein";
    public static final String PLAYLIST = "playlist";
    public static final String PLAYQUEUE = "playqueue";
    public static final String PLAYTRACK = "playtrack";
    public static final String PLAYURI = "playuri";
    public static final String PUBLICADDRESS = "publicaddress";
    public static final String PUBLICANALOGADDRESS = "publicanalogaddress";
    public static final String PUBLICDIGITALADDRESS = "publicdigitaladdress";
    public static final String RADIO = "radio";
    public static final String REMOVE = "remove";
    public static final String REPEAT = "repeat";
    public static final String RESTORE = "restore";
    public static final String RESTOREALL = "restoreall";
    public static final String SAVE = "save";
    public static final String SAVEALL = "saveall";
    public static final String SHUFFLE = "shuffle";
    public static final String SLEEPTIMER = "sleeptimer";
    public static final String SNOOZE = "snooze";
    public static final String SPEECHENHANCEMENT = "speechenhancement";
    public static final String STANDALONE = "standalone";
    public static final String STATE = "state";
    public static final String STOP = "stop";
    public static final String TREBLE = "treble";
    public static final String SUBWOOFER = "subwoofer";
    public static final String SUBWOOFERGAIN = "subwoofergain";
    public static final String SURROUND = "surround";
    public static final String SURROUNDMUSICMODE = "surroundmusicmode";
    public static final String SURROUNDMUSICLEVEL = "surroundmusiclevel";
    public static final String SURROUNDTVLEVEL = "surroundtvlevel";
    public static final String TUNEINSTATIONID = "tuneinstationid";
    public static final String VOLUME = "volume";
    public static final String ZONEGROUPID = "zonegroupid";
    public static final String ZONENAME = "zonename";
    public static final String MODELID = "modelId";

    // List of properties
    public static final String IDENTIFICATION = "identification";
    public static final String MAC_ADDRESS = "macAddress";
    public static final String IP_ADDRESS = "ipAddress";
}
