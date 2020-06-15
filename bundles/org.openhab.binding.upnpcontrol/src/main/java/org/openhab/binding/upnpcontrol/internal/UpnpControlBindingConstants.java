/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.upnpcontrol.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link UpnpControlBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class UpnpControlBindingConstants {

    public static final String BINDING_ID = "upnpcontrol";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_RENDERER = new ThingTypeUID(BINDING_ID, "upnprenderer");
    public static final ThingTypeUID THING_TYPE_SERVER = new ThingTypeUID(BINDING_ID, "upnpserver");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream.of(THING_TYPE_RENDERER, THING_TYPE_SERVER)
            .collect(Collectors.toSet());

    // List of thing parameter names
    public static final String HOST_PARAMETER = "ipAddress";
    public static final String TCP_PORT_PARAMETER = "port";
    public static final String UDN_PARAMETER = "udn";
    public static final String REFRESH_INTERVAL = "refreshInterval";

    // List of all Channel ids
    public static final String VOLUME = "volume";
    public static final String MUTE = "mute";
    public static final String CONTROL = "control";
    public static final String STOP = "stop";
    public static final String TITLE = "title";
    public static final String ALBUM = "album";
    public static final String ALBUM_ART = "albumart";
    public static final String CREATOR = "creator";
    public static final String ARTIST = "artist";
    public static final String PUBLISHER = "publisher";
    public static final String GENRE = "genre";
    public static final String TRACK_NUMBER = "tracknumber";
    public static final String TRACK_DURATION = "trackduration";
    public static final String TRACK_POSITION = "trackposition";

    public static final String UPNPRENDERER = "upnprenderer";
    public static final String CURRENTID = "currentid";
    public static final String BROWSE = "browse";
    public static final String SEARCH = "search";
    public static final String SERVE = "serve";

    // Thing config properties
    public static final String CONFIG_FILTER = "filter";
    public static final String SORT_CRITERIA = "sortcriteria";
}
