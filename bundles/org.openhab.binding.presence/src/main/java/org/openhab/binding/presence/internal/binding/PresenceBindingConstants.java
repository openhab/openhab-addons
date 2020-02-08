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
package org.openhab.binding.presence.internal.binding;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link PresenceBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Mike Dabbs - Initial contribution
 */
@NonNullByDefault
public class PresenceBindingConstants {

    private static final String BINDING_ID = "presence";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PINGDEVICE = new ThingTypeUID(BINDING_ID, "pingdevice");
    public static final ThingTypeUID THING_TYPE_TCPPORTDEVICE = new ThingTypeUID(BINDING_ID, "tcpportdevice");
    public static final ThingTypeUID THING_TYPE_SMTPDEVICE = new ThingTypeUID(BINDING_ID, "smtpdevice");

    // List of all Channel ids
    public static final String CHANNEL_ONLINE = "online";
    public static final String CHANNEL_LAST_SEEN = "lastseen";
    public static final String CHANNEL_FIRST_SEEN = "firstseen";

    // Parameters of Things
    public static final String PARAMETER_HOSTNAME = "hostname";
    public static final String PARAMETER_RETRY = "retry";
    public static final String PARAMETER_TIMEOUT = "timeout";
    public static final String PARAMETER_REFRESH_INTERVAL = "refreshInterval";

    // Properties of things
    public static final String PROPERTY_PREFERRED_INTERFACE = "preferred_interface";
    public static final String PROPERTY_PREFERRED_METHOD = "preferred_method";
    public static final String PROPERTY_ARP_STATE = "arp_state";
    public static final String PROPERTY_ICMP_STATE = "icmp_state";
    public static final String PROPERTY_DHCP_STATE = "dhcp_state";
}
