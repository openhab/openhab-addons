/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.pihole.internal.rest.model.v6;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public record StatAnswer(Queries queries, Clients client, Gravity gravity, double took) {
    record Clients(int active, int total) {

    }

    public record Gravity(int domainsBeingBlocked, int lastUpdate) {

    }

    public record Queries(int total, int blocked, double percentBlocked, int uniqueDomains, int forwarded, int cached,
            double frequency, Types types, Status status, Replies replies) {

    }

    public record Replies(@SerializedName("UNKNOWN") int unknown, //
            @SerializedName("NODATA") int nodata, //
            @SerializedName("NXDOMAIN") int nxdomain, //
            @SerializedName("CNAME") int cname, //
            @SerializedName("IP") int ip, //
            @SerializedName("DOMAIN") int domain, //
            @SerializedName("RRNAME") int rrname, //
            @SerializedName("SERVFAIL") int servfail, //
            @SerializedName("REFUSED") int refused, //
            @SerializedName("NOTIMP") int notimp, //
            @SerializedName("OTHER") int other, //
            @SerializedName("DNSSEC") int dnssec, //
            @SerializedName("NONE") int none, //
            @SerializedName("BLOB") int blob) {

        public int all() {
            return unknown + nodata + nxdomain + cname + ip + domain + rrname + servfail + refused + notimp + other
                    + dnssec + none + blob;
        }
    }

    public record Status(@SerializedName("UNKNOWN") int unknown, //
            @SerializedName("GRAVITY") int gravity, //
            @SerializedName("FORWARDED") int forwarded, //
            @SerializedName("CACHE") int cache, //
            @SerializedName("REGEX") int regex, //
            @SerializedName("DENYLIST") int denylist, //
            @SerializedName("EXTERNAL_BLOCKED_IP") int externalBlockedIp, //
            @SerializedName("EXTERNAL_BLOCKED_NULL") int externalBlockedNull, //
            @SerializedName("EXTERNAL_BLOCKED_NXRA") int externalBlockedNxra, //
            @SerializedName("GRAVITY_CNAME") int gravityCname, //
            @SerializedName("REGEX_CNAME") int regexCname, //
            @SerializedName("DENYLIST_CNAME") int denylistCname, //
            @SerializedName("RETRIED") int retried, //
            @SerializedName("RETRIED_DNSSEC") int retriedDnssec, //
            @SerializedName("IN_PROGRESS") int inProgress, //
            @SerializedName("DBBUSY") int dbbusy, //
            @SerializedName("SPECIAL_DOMAIN") int specialDomain, //
            @SerializedName("CACHE_STALE") int cacheStale, //
            @SerializedName("EXTERNAL_BLOCKED_EDE15") int externalBlockedEde15) {
    }

    public record Types(@SerializedName("A") int a, //
            @SerializedName("AAAA") int aaaa, //
            @SerializedName("ANY") int any, //
            @SerializedName("SRV") int srv, //
            @SerializedName("SOA") int soa, //
            @SerializedName("PTR") int ptr, //
            @SerializedName("TXT") int txt, //
            @SerializedName("NAPTR") int naptr, //
            @SerializedName("MX") int mx, //
            @SerializedName("DS") int ds, //
            @SerializedName("RRSIG") int rrsig, //
            @SerializedName("DNSKEY") int dnskey, //
            @SerializedName("NS") int ns, //
            @SerializedName("SVCB") int svcb, //
            @SerializedName("HTTPS") int https, //
            @SerializedName("OTHER") int other) {

        public int all() {
            return a + aaaa + any + srv + soa + ptr + txt + naptr + mx + ds + rrsig + dnskey + ns + svcb + https
                    + other;
        }
    }
}
