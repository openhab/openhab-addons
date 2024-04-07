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
package org.openhab.binding.pihole.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link PiHoleBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class PiHoleBindingConstants {

    public static final String BINDING_ID = "pihole";

    // List of all Thing Type UIDs
    public static final ThingTypeUID PI_HOLE_TYPE = new ThingTypeUID(BINDING_ID, "server");

    public static final class Channels {
        public static final String DOMAINS_BEING_BLOCKED_CHANNEL = "domains_being_blocked";
        public static final String DNS_QUERIES_TODAY_CHANNEL = "dns_queries_today";
        public static final String ADS_BLOCKED_TODAY_CHANNEL = "ads_blocked_today";
        public static final String ADS_PERCENTAGE_TODAY_CHANNEL = "ads_percentage_today";
        public static final String UNIQUE_DOMAINS_CHANNEL = "unique_domains";
        public static final String QUERIES_FORWARDED_CHANNEL = "queries_forwarded";
        public static final String QUERIES_CACHED_CHANNEL = "queries_cached";
        public static final String CLIENTS_EVER_SEEN_CHANNEL = "clients_ever_seen";
        public static final String UNIQUE_CLIENTS_CHANNEL = "unique_clients";
        public static final String DNS_QUERIES_ALL_TYPES_CHANNEL = "dns_queries_all_types";
        public static final String REPLY_UNKNOWN_CHANNEL = "reply_UNKNOWN";
        public static final String REPLY_NODATA_CHANNEL = "reply_NODATA";
        public static final String REPLY_NXDOMAIN_CHANNEL = "reply_NXDOMAIN";
        public static final String REPLY_CNAME_CHANNEL = "reply_CNAME";
        public static final String REPLY_IP_CHANNEL = "reply_IP";
        public static final String REPLY_DOMAIN_CHANNEL = "reply_DOMAIN";
        public static final String REPLY_RRNAME_CHANNEL = "reply_RRNAME";
        public static final String REPLY_SERVFAIL_CHANNEL = "reply_SERVFAIL";
        public static final String REPLY_REFUSED_CHANNEL = "reply_REFUSED";
        public static final String REPLY_NOTIMP_CHANNEL = "reply_NOTIMP";
        public static final String REPLY_OTHER_CHANNEL = "reply_OTHER";
        public static final String REPLY_DNSSEC_CHANNEL = "reply_DNSSEC";
        public static final String REPLY_NONE_CHANNEL = "reply_NONE";
        public static final String REPLY_BLOB_CHANNEL = "reply_BLOB";
        public static final String DNS_QUERIES_ALL_REPLIES_CHANNEL = "dns_queries_all_replies";
        public static final String PRIVACY_LEVEL_CHANNEL = "privacy_level";
        public static final String ENABLED_CHANNEL = "enabled";
    }
}
