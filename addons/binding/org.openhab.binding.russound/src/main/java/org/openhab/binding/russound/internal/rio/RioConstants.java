/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.russound.RussoundBindingConstants;

/**
 * The class defines common constants ({@link ThingTypeUID} and channels), which are used across the rio binding
 *
 * @author Tim Roberts
 */
public class RioConstants {

    // BRIDGE TYPE IDS
    public static final ThingTypeUID BRIDGE_TYPE_RIO = new ThingTypeUID(RussoundBindingConstants.BINDING_ID, "rio");
    public static final ThingTypeUID BRIDGE_TYPE_CONTROLLER = new ThingTypeUID(RussoundBindingConstants.BINDING_ID,
            "controller");

    public static final ThingTypeUID BRIDGE_TYPE_ZONE = new ThingTypeUID(RussoundBindingConstants.BINDING_ID, "zone");
    public static final ThingTypeUID BRIDGE_TYPE_SOURCE = new ThingTypeUID(RussoundBindingConstants.BINDING_ID,
            "source");

    public static final ThingTypeUID BRIDGE_TYPE_BANK = new ThingTypeUID(RussoundBindingConstants.BINDING_ID, "bank");

    // THING TYPE IDS
    public static final ThingTypeUID THING_TYPE_BANK_PRESET = new ThingTypeUID(RussoundBindingConstants.BINDING_ID,
            "bankpreset");
    public static final ThingTypeUID THING_TYPE_ZONE_PRESET = new ThingTypeUID(RussoundBindingConstants.BINDING_ID,
            "zonepreset");
    public static final ThingTypeUID THING_TYPE_SYSTEM_FAVORITE = new ThingTypeUID(RussoundBindingConstants.BINDING_ID,
            "sysfavorite");
    public static final ThingTypeUID THING_TYPE_ZONE_FAVORITE = new ThingTypeUID(RussoundBindingConstants.BINDING_ID,
            "zonefavorite");

    // SYSTEM PROPERTIES
    public static final String PROPERTY_SYSVERSION = "Firmware Version";

    // SYSTEM CHANNELS
    public static final String CHANNEL_SYSSTATUS = "status"; // readonly
    public static final String CHANNEL_SYSLANG = "lang"; // read/write - english, chinese, russian
    public static final String CHANNEL_SYSALLON = "allon"; // read/write - english, chinese, russian

    // CONTROLLER PROPERTIES
    public static final String PROPERTY_CTLTYPE = "Model Type";
    public static final String PROPERTY_CTLIPADDRESS = "IP Address";
    public static final String PROPERTY_CTLMACADDRESS = "MAC Address";

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

    // ZONE EVENT BASED
    public static final String CHANNEL_ZONEKEYPRESS = "keypress";
    public static final String CHANNEL_ZONEKEYRELEASE = "keyrelease";
    public static final String CHANNEL_ZONEKEYHOLD = "keyhold";
    public static final String CHANNEL_ZONEKEYCODE = "keycode";
    public static final String CHANNEL_ZONEEVENT = "event";

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
    public static final String PROPERTY_SOURCETYPE = "Source Type";
    public static final String PROPERTY_SOURCEIPADDRESS = "IP Address";

    // SOURCE CHANNELS
    public static final String CHANNEL_SOURCENAME = "name";
    public static final String CHANNEL_SOURCECOMPOSERNAME = "composername";
    public static final String CHANNEL_SOURCECHANNEL = "channel";
    public static final String CHANNEL_SOURCECHANNELNAME = "channelname";
    public static final String CHANNEL_SOURCEGENRE = "genre";
    public static final String CHANNEL_SOURCEARTISTNAME = "artistname";
    public static final String CHANNEL_SOURCEALBUMNAME = "albumname";
    public static final String CHANNEL_SOURCECOVERARTURL = "coverarturl";
    public static final String CHANNEL_SOURCECOVERART = "coverart";
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
}
