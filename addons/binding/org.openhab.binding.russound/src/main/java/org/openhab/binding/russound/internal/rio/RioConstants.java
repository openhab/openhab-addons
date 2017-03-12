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
    public final static ThingTypeUID BRIDGE_TYPE_RIO = new ThingTypeUID(RussoundBindingConstants.BINDING_ID, "rio");
    public final static ThingTypeUID BRIDGE_TYPE_CONTROLLER = new ThingTypeUID(RussoundBindingConstants.BINDING_ID,
            "controller");

    public final static ThingTypeUID BRIDGE_TYPE_ZONE = new ThingTypeUID(RussoundBindingConstants.BINDING_ID, "zone");
    public final static ThingTypeUID BRIDGE_TYPE_SOURCE = new ThingTypeUID(RussoundBindingConstants.BINDING_ID,
            "source");

    public final static ThingTypeUID BRIDGE_TYPE_BANK = new ThingTypeUID(RussoundBindingConstants.BINDING_ID, "bank");

    // THING TYPE IDS
    public final static ThingTypeUID THING_TYPE_BANK_PRESET = new ThingTypeUID(RussoundBindingConstants.BINDING_ID,
            "bankpreset");
    public final static ThingTypeUID THING_TYPE_ZONE_PRESET = new ThingTypeUID(RussoundBindingConstants.BINDING_ID,
            "zonepreset");
    public final static ThingTypeUID THING_TYPE_SYSTEM_FAVORITE = new ThingTypeUID(RussoundBindingConstants.BINDING_ID,
            "sysfavorite");
    public final static ThingTypeUID THING_TYPE_ZONE_FAVORITE = new ThingTypeUID(RussoundBindingConstants.BINDING_ID,
            "zonefavorite");

    // SYSTEM PROPERTIES
    public final static String PROPERTY_SYSVERSION = "Firmware Version";

    // SYSTEM CHANNELS
    public final static String CHANNEL_SYSSTATUS = "status"; // readonly
    public final static String CHANNEL_SYSLANG = "lang"; // read/write - english, chinese, russian
    public final static String CHANNEL_SYSALLON = "allon"; // read/write - english, chinese, russian

    // CONTROLLER PROPERTIES
    public final static String PROPERTY_CTLTYPE = "Model Type";
    public final static String PROPERTY_CTLIPADDRESS = "IP Address";
    public final static String PROPERTY_CTLMACADDRESS = "MAC Address";

    // ZONE CHANNELS
    public final static String CHANNEL_ZONENAME = "name"; // 12 max
    public final static String CHANNEL_ZONESOURCE = "source"; // 1-8 or 1-12
    public final static String CHANNEL_ZONEBASS = "bass"; // -10 to 10
    public final static String CHANNEL_ZONETREBLE = "treble"; // -10 to 10
    public final static String CHANNEL_ZONEBALANCE = "balance"; // -10 to 10
    public final static String CHANNEL_ZONELOUDNESS = "loudness"; // OFF/ON
    public final static String CHANNEL_ZONETURNONVOLUME = "turnonvolume"; // 0 to 50
    public final static String CHANNEL_ZONEDONOTDISTURB = "donotdisturb"; // OFF/ON/SLAVE
    public final static String CHANNEL_ZONEPARTYMODE = "partymode"; // OFF/ON/MASTER
    public final static String CHANNEL_ZONESTATUS = "status"; // OFF/ON/MASTER
    public final static String CHANNEL_ZONEVOLUME = "volume"; // OFF/ON/MASTER
    public final static String CHANNEL_ZONEMUTE = "mute"; // OFF/ON/MASTER
    public final static String CHANNEL_ZONEPAGE = "page"; // OFF/ON/MASTER
    public final static String CHANNEL_ZONERATING = "rating"; // OFF=Dislike, On=Like
    public final static String CHANNEL_ZONESHAREDSOURCE = "sharedsource"; // OFF/ON/MASTER
    public final static String CHANNEL_ZONESLEEPTIMEREMAINING = "sleeptimeremaining"; // OFF/ON/MASTER
    public final static String CHANNEL_ZONELASTERROR = "lasterror"; // OFF/ON/MASTER
    public final static String CHANNEL_ZONEENABLED = "enabled"; // OFF/ON/MASTER
    public final static String CHANNEL_ZONEREPEAT = "repeat"; // OFF/ON/MASTER
    public final static String CHANNEL_ZONESHUFFLE = "shuffle"; // OFF/ON/MASTER

    // ZONE EVENT BASED
    public final static String CHANNEL_ZONEKEYPRESS = "keypress";
    public final static String CHANNEL_ZONEKEYRELEASE = "keyrelease";
    public final static String CHANNEL_ZONEKEYHOLD = "keyhold";
    public final static String CHANNEL_ZONEKEYCODE = "keycode";
    public final static String CHANNEL_ZONEEVENT = "event";

    // FAVORITE CHANNELS
    public final static String CHANNEL_FAVNAME = "name";
    public final static String CHANNEL_FAVVALID = "valid";
    public final static String CHANNEL_FAVCMD = "cmd";

    // FAVORITE COMMANDS
    public final static String CMD_FAVSAVESYS = "savesystem";
    public final static String CMD_FAVRESTORESYS = "restoresystem";
    public final static String CMD_FAVDELETESYS = "deletesystem";
    public final static String CMD_FAVSAVEZONE = "savezone";
    public final static String CMD_FAVRESTOREZONE = "restorezone";
    public final static String CMD_FAVDELETEZONE = "deletezone";

    // BANK CHANNELS
    public final static String CHANNEL_BANKNAME = "name";

    // PRESET CHANNELS
    public final static String CHANNEL_PRESETNAME = "name";
    public final static String CHANNEL_PRESETVALID = "valid";
    public final static String CHANNEL_PRESETCMD = "cmd";

    // PRESET COMMANDS
    public final static String CMD_PRESETSAVE = "save";
    public final static String CMD_PRESETRESTORE = "restore";
    public final static String CMD_PRESETDELETE = "delete";

    // SOURCE PROPERTIES
    public final static String PROPERTY_SOURCETYPE = "Source Type";
    public final static String PROPERTY_SOURCEIPADDRESS = "IP Address";

    // SOURCE CHANNELS
    public final static String CHANNEL_SOURCENAME = "name";
    public final static String CHANNEL_SOURCECOMPOSERNAME = "composername";
    public final static String CHANNEL_SOURCECHANNEL = "channel";
    public final static String CHANNEL_SOURCECHANNELNAME = "channelname";
    public final static String CHANNEL_SOURCEGENRE = "genre";
    public final static String CHANNEL_SOURCEARTISTNAME = "artistname";
    public final static String CHANNEL_SOURCEALBUMNAME = "albumname";
    public final static String CHANNEL_SOURCECOVERARTURL = "coverarturl";
    public final static String CHANNEL_SOURCECOVERART = "coverart";
    public final static String CHANNEL_SOURCEPLAYLISTNAME = "playlistname";
    public final static String CHANNEL_SOURCESONGNAME = "songname";
    public final static String CHANNEL_SOURCEMODE = "mode";
    public final static String CHANNEL_SOURCESHUFFLEMODE = "shufflemode";
    public final static String CHANNEL_SOURCEREPEATMODE = "repeatmode";
    public final static String CHANNEL_SOURCERATING = "rating";
    public final static String CHANNEL_SOURCEPROGRAMSERVICENAME = "programservicename";
    public final static String CHANNEL_SOURCERADIOTEXT = "radiotext";
    public final static String CHANNEL_SOURCERADIOTEXT2 = "radiotext2";
    public final static String CHANNEL_SOURCERADIOTEXT3 = "radiotext3";
    public final static String CHANNEL_SOURCERADIOTEXT4 = "radiotext4";
    public final static String CHANNEL_SOURCEVOLUME = "volume";
}
