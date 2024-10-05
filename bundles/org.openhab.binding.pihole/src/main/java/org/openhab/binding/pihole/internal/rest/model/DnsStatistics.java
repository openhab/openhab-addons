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
package org.openhab.binding.pihole.internal.rest.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public record DnsStatistics(@Nullable Integer domainsBeingBlocked, @Nullable Integer dnsQueriesToday,
        @Nullable Integer adsBlockedToday, @Nullable Double adsPercentageToday, @Nullable Integer uniqueDomains,
        @Nullable Integer queriesForwarded, @Nullable Integer queriesCached, @Nullable Integer clientsEverSeen,
        @Nullable Integer uniqueClients, @Nullable Integer dnsQueriesAllTypes,
        @SerializedName("reply_UNKNOWN") @Nullable Integer replyUnknown,
        @SerializedName("reply_NODATA") @Nullable Integer replyNoData,
        @SerializedName("reply_NXDOMAIN") @Nullable Integer replyNXDomain,
        @SerializedName("reply_CNAME") @Nullable Integer replyCName,
        @SerializedName("reply_IP") @Nullable Integer replyIP,
        @SerializedName("reply_DOMAIN") @Nullable Integer replyDomain,
        @SerializedName("reply_RRNAME") @Nullable Integer replyRRName,
        @SerializedName("reply_SERVFAIL") @Nullable Integer replyServFail,
        @SerializedName("reply_REFUSED") @Nullable Integer replyRefused,
        @SerializedName("reply_NOTIMP") @Nullable Integer replyNotImp,
        @SerializedName("reply_OTHER") @Nullable Integer replyOther,
        @SerializedName("reply_DNSSEC") @Nullable Integer replyDNSSEC,
        @SerializedName("reply_NONE") @Nullable Integer replyNone,
        @SerializedName("reply_BLOB") @Nullable Integer replyBlob, @Nullable Integer dnsQueriesAllReplies,
        @Nullable Integer privacyLevel, @Nullable String status, @Nullable GravityLastUpdated gravityLastUpdated) {
    public boolean enabled() {
        return "enabled".equalsIgnoreCase(status);
    }
}
