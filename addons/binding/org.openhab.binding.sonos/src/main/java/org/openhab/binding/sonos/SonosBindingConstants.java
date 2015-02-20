/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonos;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SonosBinding} class defines common constants, which are 
 * used across the whole binding.
 * 
 * @author Karel Goderis - Initial contribution
 */
public class SonosBindingConstants {

    public static final String BINDING_ID = "sonos";
    
    // List of all Thing Type UIDs
    public final static ThingTypeUID ZONEPLAYER_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "zoneplayer");

    // List of all Channel ids
    public final static String ADD = "add";
    public final static String ALARM = "alarm";
    public final static String ALARMPROPERTIES = "alarmproperties";
    public final static String ALARMRUNNING = "alarmrunning";
    public final static String CONTROL ="control";
    public final static String CURRENTALBUM = "currentalbum";
    public final static String CURRENTARTIST = "currentartist";
    public final static String CURRENTTITLE = "currenttitle";
    public final static String CURRENTTRACK = "currenttrack";
    public final static String LED = "led";
    public final static String LINEIN = "linein";
    public final static String LOCALCOORDINATOR = "localcoordinator";
    public final static String MUTE = "mute";
    public final static String PLAYLINEIN = "playlinein";
    public final static String PLAYLIST = "playlist";
    public final static String PLAYQUEUE = "playqueue";
    public final static String PLAYTRACK = "playtrack";
    public final static String PLAYURI = "playuri";
    public final static String PUBLICADDRESS = "publicaddress";
    public final static String RADIO = "radio";
    public final static String REMOVE = "remove";
    public final static String RESTORE = "restore";
    public final static String RESTOREALL = "restoreall";
    public final static String SAVE = "save";
    public final static String SAVEALL = "saveall";
    public final static String SNOOZE = "snooze";
    public final static String STANDALONE = "standalone";
    public final static String STATE = "state";
    public final static String STOP = "stop";
    public final static String VOLUME = "volume";
    public final static String ZONEGROUP = "zonegroup";
    public final static String ZONEGROUPID = "zonegroupid";
    public final static String ZONENAME = "zonename";
    

}
