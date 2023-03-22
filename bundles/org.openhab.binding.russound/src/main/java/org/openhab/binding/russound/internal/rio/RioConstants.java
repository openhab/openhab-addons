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
package org.openhab.binding.russound.internal.rio;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.russound.internal.RussoundBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The class defines common constants ({@link ThingTypeUID} and channels), which are used across the rio binding
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class RioConstants {

    // BRIDGE TYPE IDS
    public static final ThingTypeUID BRIDGE_TYPE_RIO = new ThingTypeUID(RussoundBindingConstants.BINDING_ID, "rio");
    public static final ThingTypeUID BRIDGE_TYPE_CONTROLLER = new ThingTypeUID(RussoundBindingConstants.BINDING_ID,
            "controller");

    public static final ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(RussoundBindingConstants.BINDING_ID, "zone");
    public static final ThingTypeUID THING_TYPE_SOURCE = new ThingTypeUID(RussoundBindingConstants.BINDING_ID,
            "source");

    // the port number rio listens on
    public static final int RIO_PORT = 9621;

    // SYSTEM PROPERTIES
    public static final String PROPERTY_SYSVERSION = "Firmware Version";

    // SYSTEM CHANNELS
    public static final String CHANNEL_SYSSTATUS = "status"; // readonly
    public static final String CHANNEL_SYSLANG = "lang"; // read/write - english, chinese, russian
    public static final String CHANNEL_SYSALLON = "allon"; // read/write - english, chinese, russian
    public static final String CHANNEL_SYSCONTROLLERS = "controllers"; // json array [1,2,etc]
    public static final String CHANNEL_SYSSOURCES = "sources"; // json array [{id: 1, name: xxx},{id:2, name: xxx}, etc]

    // CONTROLLER PROPERTIES
    public static final String PROPERTY_CTLTYPE = "Model Type";
    public static final String PROPERTY_CTLIPADDRESS = "IP Address";
    public static final String PROPERTY_CTLMACADDRESS = "MAC Address";

    // CONTROLLER CHANNELS
    public static final String CHANNEL_CTLZONES = "zones"; // json array [{id: 1, name: xxx},{id:2, name: xxx}, etc]

    // ZONE CHANNELS
    public static final String CHANNEL_ZONENAME = "name"; // 12 max
    public static final String CHANNEL_ZONESOURCE = "source"; // 1-8 or 1-12
    public static final String CHANNEL_ZONEBASS = "bass"; // -10 to 10
    public static final String CHANNEL_ZONETREBLE = "treble"; // -10 to 10
    public static final String CHANNEL_ZONEBALANCE = "balance"; // -10 to 10
    public static final String CHANNEL_ZONELOUDNESS = "loudness"; // OFF/ON
    public static final String CHANNEL_ZONETURNONVOLUME = "turnonvolume"; // 0 to 50
    public static final String CHANNEL_ZONEDONOTDISTURB = "donotdisturb"; // OFF/ON/SLAVE
    public static final String CHANNEL_ZONEPARTYMODE = "partymode"; // OFF/ON/MASTER
    public static final String CHANNEL_ZONESTATUS = "status"; // OFF/ON/MASTER
    public static final String CHANNEL_ZONEVOLUME = "volume"; // OFF/ON/MASTER
    public static final String CHANNEL_ZONEMUTE = "mute"; // OFF/ON/MASTER
    public static final String CHANNEL_ZONEPAGE = "page"; // OFF/ON/MASTER
    public static final String CHANNEL_ZONERATING = "rating"; // OFF=Dislike, On=Like
    public static final String CHANNEL_ZONESHAREDSOURCE = "sharedsource"; // OFF/ON/MASTER
    public static final String CHANNEL_ZONESLEEPTIMEREMAINING = "sleeptimeremaining"; // OFF/ON/MASTER
    public static final String CHANNEL_ZONELASTERROR = "lasterror"; // OFF/ON/MASTER
    public static final String CHANNEL_ZONEENABLED = "enabled"; // OFF/ON/MASTER
    public static final String CHANNEL_ZONEREPEAT = "repeat"; // OFF/ON/MASTER
    public static final String CHANNEL_ZONESHUFFLE = "shuffle"; // OFF/ON/MASTER

    public static final String CHANNEL_ZONESYSFAVORITES = "systemfavorites"; // json array
    public static final String CHANNEL_ZONEFAVORITES = "zonefavorites"; // json array
    public static final String CHANNEL_ZONEPRESETS = "presets"; // json array

    // ZONE EVENT BASED
    public static final String CHANNEL_ZONEKEYPRESS = "keypress";
    public static final String CHANNEL_ZONEKEYRELEASE = "keyrelease";
    public static final String CHANNEL_ZONEKEYHOLD = "keyhold";
    public static final String CHANNEL_ZONEKEYCODE = "keycode";
    public static final String CHANNEL_ZONEEVENT = "event";

    // ZONE MEDIA CHANNELS
    public static final String CHANNEL_ZONEMMINIT = "mminit";
    public static final String CHANNEL_ZONEMMCONTEXTMENU = "mmcontextmenu";

    // FAVORITE CHANNELS
    public static final String CHANNEL_FAVNAME = "name";
    public static final String CHANNEL_FAVVALID = "valid";
    public static final String CHANNEL_FAVCMD = "cmd";

    // FAVORITE COMMANDS
    public static final String CMD_FAVSAVESYS = "savesystem";
    public static final String CMD_FAVRESTORESYS = "restoresystem";
    public static final String CMD_FAVDELETESYS = "deletesystem";
    public static final String CMD_FAVSAVEZONE = "savezone";
    public static final String CMD_FAVRESTOREZONE = "restorezone";
    public static final String CMD_FAVDELETEZONE = "deletezone";

    // BANK CHANNELS
    public static final String CHANNEL_BANKNAME = "name";

    // PRESET CHANNELS
    public static final String CHANNEL_PRESETNAME = "name";
    public static final String CHANNEL_PRESETVALID = "valid";
    public static final String CHANNEL_PRESETCMD = "cmd";

    // PRESET COMMANDS
    public static final String CMD_PRESETSAVE = "save";
    public static final String CMD_PRESETRESTORE = "restore";
    public static final String CMD_PRESETDELETE = "delete";

    // SOURCE PROPERTIES
    public static final String PROPERTY_SOURCEIPADDRESS = "IP Address";

    // SOURCE CHANNELS
    public static final String CHANNEL_SOURCETYPE = "type";
    public static final String CHANNEL_SOURCENAME = "name";
    public static final String CHANNEL_SOURCECOMPOSERNAME = "composername";
    public static final String CHANNEL_SOURCECHANNEL = "channel";
    public static final String CHANNEL_SOURCECHANNELNAME = "channelname";
    public static final String CHANNEL_SOURCEGENRE = "genre";
    public static final String CHANNEL_SOURCEARTISTNAME = "artistname";
    public static final String CHANNEL_SOURCEALBUMNAME = "albumname";
    public static final String CHANNEL_SOURCECOVERARTURL = "coverarturl";
    public static final String CHANNEL_SOURCEPLAYLISTNAME = "playlistname";
    public static final String CHANNEL_SOURCESONGNAME = "songname";
    public static final String CHANNEL_SOURCEMODE = "mode";
    public static final String CHANNEL_SOURCESHUFFLEMODE = "shufflemode";
    public static final String CHANNEL_SOURCEREPEATMODE = "repeatmode";
    public static final String CHANNEL_SOURCERATING = "rating";
    public static final String CHANNEL_SOURCEPROGRAMSERVICENAME = "programservicename";
    public static final String CHANNEL_SOURCERADIOTEXT = "radiotext";
    public static final String CHANNEL_SOURCERADIOTEXT2 = "radiotext2";
    public static final String CHANNEL_SOURCERADIOTEXT3 = "radiotext3";
    public static final String CHANNEL_SOURCERADIOTEXT4 = "radiotext4";
    public static final String CHANNEL_SOURCEVOLUME = "volume";

    public static final String CHANNEL_SOURCEBANKS = "banks";

    // SOURCE MEDIA Channels
    public static final String CHANNEL_SOURCEMMSCREEN = "mmscreen";
    public static final String CHANNEL_SOURCEMMTITLE = "mmtitle";
    public static final String CHANNEL_SOURCEMMMENU = "mmmenu";
    public static final String CHANNEL_SOURCEMMATTR = "mmattr";
    public static final String CHANNEL_SOURCEMMBUTTONOKTEXT = "mmmenubuttonoktext";
    public static final String CHANNEL_SOURCEMMBUTTONBACKTEXT = "mmmenubuttonbacktext";
    public static final String CHANNEL_SOURCEMMINFOTEXT = "mminfotext";
    public static final String CHANNEL_SOURCEMMHELPTEXT = "mmhelptext";
    public static final String CHANNEL_SOURCEMMTEXTFIELD = "mmtextfield";
}
