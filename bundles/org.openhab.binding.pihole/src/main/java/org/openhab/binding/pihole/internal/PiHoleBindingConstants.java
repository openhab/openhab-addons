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
        public static final String DOMAINS_BEING_BLOCKED_CHANNEL = "domains-being-blocked";
        public static final String DNS_QUERIES_TODAY_CHANNEL = "dns-queries-today";
        public static final String ADS_BLOCKED_TODAY_CHANNEL = "ads-blocked-today";
        public static final String ADS_PERCENTAGE_TODAY_CHANNEL = "ads-percentage-today";
        public static final String UNIQUE_DOMAINS_CHANNEL = "unique-domains";
        public static final String QUERIES_FORWARDED_CHANNEL = "queries-forwarded";
        public static final String QUERIES_CACHED_CHANNEL = "queries-cached";
        public static final String CLIENTS_EVER_SEEN_CHANNEL = "clients-ever-seen";
        public static final String UNIQUE_CLIENTS_CHANNEL = "unique-clients";
        public static final String DNS_QUERIES_ALL_TYPES_CHANNEL = "dns-queries-all-types";
        public static final String REPLY_UNKNOWN_CHANNEL = "reply-unknown";
        public static final String REPLY_NODATA_CHANNEL = "reply-nodata";
        public static final String REPLY_NXDOMAIN_CHANNEL = "reply-nxdomain";
        public static final String REPLY_CNAME_CHANNEL = "reply-cname";
        public static final String REPLY_IP_CHANNEL = "reply-ip";
        public static final String REPLY_DOMAIN_CHANNEL = "reply-domain";
        public static final String REPLY_RRNAME_CHANNEL = "reply-rrname";
        public static final String REPLY_SERVFAIL_CHANNEL = "reply-servfail";
        public static final String REPLY_REFUSED_CHANNEL = "reply-refused";
        public static final String REPLY_NOTIMP_CHANNEL = "reply-notimp";
        public static final String REPLY_OTHER_CHANNEL = "reply-other";
        public static final String REPLY_DNSSEC_CHANNEL = "reply-dnssec";
        public static final String REPLY_NONE_CHANNEL = "reply-none";
        public static final String REPLY_BLOB_CHANNEL = "reply-blob";
        public static final String DNS_QUERIES_ALL_REPLIES_CHANNEL = "dns-queries-all-replies";
        public static final String PRIVACY_LEVEL_CHANNEL = "privacy-level";
        public static final String ENABLED_CHANNEL = "enabled";
        public static final String DISABLE_ENABLE_CHANNEL = "disable-enable";
        public static final String GRAVITY_FILE_EXISTS = "gravity-file-exists";
        public static final String GRAVITY_LAST_UPDATE = "gravity-last-update";

        public static enum DisableEnable {
            DISABLE,
            FOR_10_SEC,
            FOR_30_SEC,
            FOR_5_MIN,
            ENABLE
        }
    }
}
