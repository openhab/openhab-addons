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

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class DnsStatistics {

    @SerializedName("domains_being_blocked")
    @Nullable
    private Integer domainsBeingBlocked;
    @SerializedName("dns_queries_today")
    @Nullable
    private Integer dnsQueriesToday;
    @SerializedName("ads_blocked_today")
    @Nullable
    private Integer adsBlockedToday;
    @SerializedName("ads_percentage_today")
    @Nullable
    private Double adsPercentageToday;
    @SerializedName("unique_domains")
    @Nullable
    private Integer uniqueDomains;
    @SerializedName("queries_forwarded")
    @Nullable
    private Integer queriesForwarded;
    @SerializedName("queries_cached")
    @Nullable
    private Integer queriesCached;
    @SerializedName("clients_ever_seen")
    @Nullable
    private Integer clientsEverSeen;
    @SerializedName("unique_clients")
    @Nullable
    private Integer uniqueClients;
    @SerializedName("dns_queries_all_types")
    @Nullable
    private Integer dnsQueriesAllTypes;
    @SerializedName("reply_UNKNOWN")
    @Nullable
    private Integer replyUnknown;
    @SerializedName("reply_NODATA")
    @Nullable
    private Integer replyNoData;
    @SerializedName("reply_NXDOMAIN")
    @Nullable
    private Integer replyNXDomain;
    @SerializedName("reply_CNAME")
    @Nullable
    private Integer replyCName;
    @SerializedName("reply_IP")
    @Nullable
    private Integer replyIP;
    @SerializedName("reply_DOMAIN")
    @Nullable
    private Integer replyDomain;
    @SerializedName("reply_RRNAME")
    @Nullable
    private Integer replyRRName;
    @SerializedName("reply_SERVFAIL")
    @Nullable
    private Integer replyServFail;
    @SerializedName("reply_REFUSED")
    @Nullable
    private Integer replyRefused;
    @SerializedName("reply_NOTIMP")
    @Nullable
    private Integer replyNotImp;
    @SerializedName("reply_OTHER")
    @Nullable
    private Integer replyOther;
    @SerializedName("reply_DNSSEC")
    @Nullable
    private Integer replyDNSSEC;
    @SerializedName("reply_NONE")
    @Nullable
    private Integer replyNone;
    @SerializedName("reply_BLOB")
    @Nullable
    private Integer replyBlob;
    @SerializedName("dns_queries_all_replies")
    @Nullable
    private Integer dnsQueriesAllReplies;
    @SerializedName("privacy_level")
    @Nullable
    private Integer privacyLevel;
    @Nullable
    private String status;
    @SerializedName("gravity_last_updated")
    @Nullable
    private GravityLastUpdated gravityLastUpdated;

    public DnsStatistics() {
    }

    public DnsStatistics(Integer domainsBeingBlocked, Integer dnsQueriesToday, Integer adsBlockedToday,
            Double adsPercentageToday, Integer uniqueDomains, Integer queriesForwarded, Integer queriesCached,
            Integer clientsEverSeen, Integer uniqueClients, Integer dnsQueriesAllTypes, Integer replyUnknown,
            Integer replyNoData, Integer replyNXDomain, Integer replyCName, Integer replyIP, Integer replyDomain,
            Integer replyRRName, Integer replyServFail, Integer replyRefused, Integer replyNotImp, Integer replyOther,
            Integer replyDNSSEC, Integer replyNone, Integer replyBlob, Integer dnsQueriesAllReplies,
            Integer privacyLevel, String status, GravityLastUpdated gravityLastUpdated) {
        this.domainsBeingBlocked = domainsBeingBlocked;
        this.dnsQueriesToday = dnsQueriesToday;
        this.adsBlockedToday = adsBlockedToday;
        this.adsPercentageToday = adsPercentageToday;
        this.uniqueDomains = uniqueDomains;
        this.queriesForwarded = queriesForwarded;
        this.queriesCached = queriesCached;
        this.clientsEverSeen = clientsEverSeen;
        this.uniqueClients = uniqueClients;
        this.dnsQueriesAllTypes = dnsQueriesAllTypes;
        this.replyUnknown = replyUnknown;
        this.replyNoData = replyNoData;
        this.replyNXDomain = replyNXDomain;
        this.replyCName = replyCName;
        this.replyIP = replyIP;
        this.replyDomain = replyDomain;
        this.replyRRName = replyRRName;
        this.replyServFail = replyServFail;
        this.replyRefused = replyRefused;
        this.replyNotImp = replyNotImp;
        this.replyOther = replyOther;
        this.replyDNSSEC = replyDNSSEC;
        this.replyNone = replyNone;
        this.replyBlob = replyBlob;
        this.dnsQueriesAllReplies = dnsQueriesAllReplies;
        this.privacyLevel = privacyLevel;
        this.status = status;
        this.gravityLastUpdated = gravityLastUpdated;
    }

    public Integer getDomainsBeingBlocked() {
        return domainsBeingBlocked;
    }

    public void setDomainsBeingBlocked(Integer domainsBeingBlocked) {
        this.domainsBeingBlocked = domainsBeingBlocked;
    }

    public Integer getDnsQueriesToday() {
        return dnsQueriesToday;
    }

    public void setDnsQueriesToday(Integer dnsQueriesToday) {
        this.dnsQueriesToday = dnsQueriesToday;
    }

    public Integer getAdsBlockedToday() {
        return adsBlockedToday;
    }

    public void setAdsBlockedToday(Integer adsBlockedToday) {
        this.adsBlockedToday = adsBlockedToday;
    }

    public Double getAdsPercentageToday() {
        return adsPercentageToday;
    }

    public void setAdsPercentageToday(Double adsPercentageToday) {
        this.adsPercentageToday = adsPercentageToday;
    }

    public Integer getUniqueDomains() {
        return uniqueDomains;
    }

    public void setUniqueDomains(Integer uniqueDomains) {
        this.uniqueDomains = uniqueDomains;
    }

    public Integer getQueriesForwarded() {
        return queriesForwarded;
    }

    public void setQueriesForwarded(Integer queriesForwarded) {
        this.queriesForwarded = queriesForwarded;
    }

    public Integer getQueriesCached() {
        return queriesCached;
    }

    public void setQueriesCached(Integer queriesCached) {
        this.queriesCached = queriesCached;
    }

    public Integer getClientsEverSeen() {
        return clientsEverSeen;
    }

    public void setClientsEverSeen(Integer clientsEverSeen) {
        this.clientsEverSeen = clientsEverSeen;
    }

    public Integer getUniqueClients() {
        return uniqueClients;
    }

    public void setUniqueClients(Integer uniqueClients) {
        this.uniqueClients = uniqueClients;
    }

    public Integer getDnsQueriesAllTypes() {
        return dnsQueriesAllTypes;
    }

    public void setDnsQueriesAllTypes(Integer dnsQueriesAllTypes) {
        this.dnsQueriesAllTypes = dnsQueriesAllTypes;
    }

    public Integer getReplyUnknown() {
        return replyUnknown;
    }

    public void setReplyUnknown(Integer replyUnknown) {
        this.replyUnknown = replyUnknown;
    }

    public Integer getReplyNoData() {
        return replyNoData;
    }

    public void setReplyNoData(Integer replyNoData) {
        this.replyNoData = replyNoData;
    }

    public Integer getReplyNXDomain() {
        return replyNXDomain;
    }

    public void setReplyNXDomain(Integer replyNXDomain) {
        this.replyNXDomain = replyNXDomain;
    }

    public Integer getReplyCName() {
        return replyCName;
    }

    public void setReplyCName(Integer replyCName) {
        this.replyCName = replyCName;
    }

    public Integer getReplyIP() {
        return replyIP;
    }

    public void setReplyIP(Integer replyIP) {
        this.replyIP = replyIP;
    }

    public Integer getReplyDomain() {
        return replyDomain;
    }

    public void setReplyDomain(Integer replyDomain) {
        this.replyDomain = replyDomain;
    }

    public Integer getReplyRRName() {
        return replyRRName;
    }

    public void setReplyRRName(Integer replyRRName) {
        this.replyRRName = replyRRName;
    }

    public Integer getReplyServFail() {
        return replyServFail;
    }

    public void setReplyServFail(Integer replyServFail) {
        this.replyServFail = replyServFail;
    }

    public Integer getReplyRefused() {
        return replyRefused;
    }

    public void setReplyRefused(Integer replyRefused) {
        this.replyRefused = replyRefused;
    }

    public Integer getReplyNotImp() {
        return replyNotImp;
    }

    public void setReplyNotImp(Integer replyNotImp) {
        this.replyNotImp = replyNotImp;
    }

    public Integer getReplyOther() {
        return replyOther;
    }

    public void setReplyOther(Integer replyOther) {
        this.replyOther = replyOther;
    }

    public Integer getReplyDNSSEC() {
        return replyDNSSEC;
    }

    public void setReplyDNSSEC(Integer replyDNSSEC) {
        this.replyDNSSEC = replyDNSSEC;
    }

    public Integer getReplyNone() {
        return replyNone;
    }

    public void setReplyNone(Integer replyNone) {
        this.replyNone = replyNone;
    }

    public Integer getReplyBlob() {
        return replyBlob;
    }

    public void setReplyBlob(Integer replyBlob) {
        this.replyBlob = replyBlob;
    }

    public Integer getDnsQueriesAllReplies() {
        return dnsQueriesAllReplies;
    }

    public void setDnsQueriesAllReplies(Integer dnsQueriesAllReplies) {
        this.dnsQueriesAllReplies = dnsQueriesAllReplies;
    }

    public Integer getPrivacyLevel() {
        return privacyLevel;
    }

    public void setPrivacyLevel(Integer privacyLevel) {
        this.privacyLevel = privacyLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public GravityLastUpdated getGravityLastUpdated() {
        return gravityLastUpdated;
    }

    public void setGravityLastUpdated(GravityLastUpdated gravityLastUpdated) {
        this.gravityLastUpdated = gravityLastUpdated;
    }

    public boolean getEnabled() {
        return "enabled".equalsIgnoreCase(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DnsStatistics that = (DnsStatistics) o;

        if (!Objects.equals(domainsBeingBlocked, that.domainsBeingBlocked))
            return false;
        if (!Objects.equals(dnsQueriesToday, that.dnsQueriesToday))
            return false;
        if (!Objects.equals(adsBlockedToday, that.adsBlockedToday))
            return false;
        if (!Objects.equals(adsPercentageToday, that.adsPercentageToday))
            return false;
        if (!Objects.equals(uniqueDomains, that.uniqueDomains))
            return false;
        if (!Objects.equals(queriesForwarded, that.queriesForwarded))
            return false;
        if (!Objects.equals(queriesCached, that.queriesCached))
            return false;
        if (!Objects.equals(clientsEverSeen, that.clientsEverSeen))
            return false;
        if (!Objects.equals(uniqueClients, that.uniqueClients))
            return false;
        if (!Objects.equals(dnsQueriesAllTypes, that.dnsQueriesAllTypes))
            return false;
        if (!Objects.equals(replyUnknown, that.replyUnknown))
            return false;
        if (!Objects.equals(replyNoData, that.replyNoData))
            return false;
        if (!Objects.equals(replyNXDomain, that.replyNXDomain))
            return false;
        if (!Objects.equals(replyCName, that.replyCName))
            return false;
        if (!Objects.equals(replyIP, that.replyIP))
            return false;
        if (!Objects.equals(replyDomain, that.replyDomain))
            return false;
        if (!Objects.equals(replyRRName, that.replyRRName))
            return false;
        if (!Objects.equals(replyServFail, that.replyServFail))
            return false;
        if (!Objects.equals(replyRefused, that.replyRefused))
            return false;
        if (!Objects.equals(replyNotImp, that.replyNotImp))
            return false;
        if (!Objects.equals(replyOther, that.replyOther))
            return false;
        if (!Objects.equals(replyDNSSEC, that.replyDNSSEC))
            return false;
        if (!Objects.equals(replyNone, that.replyNone))
            return false;
        if (!Objects.equals(replyBlob, that.replyBlob))
            return false;
        if (!Objects.equals(dnsQueriesAllReplies, that.dnsQueriesAllReplies))
            return false;
        if (!Objects.equals(privacyLevel, that.privacyLevel))
            return false;
        if (!Objects.equals(status, that.status))
            return false;
        return Objects.equals(gravityLastUpdated, that.gravityLastUpdated);
    }

    @Override
    public int hashCode() {
        int result = domainsBeingBlocked != null ? domainsBeingBlocked.hashCode() : 0;
        result = 31 * result + (dnsQueriesToday != null ? dnsQueriesToday.hashCode() : 0);
        result = 31 * result + (adsBlockedToday != null ? adsBlockedToday.hashCode() : 0);
        result = 31 * result + (adsPercentageToday != null ? adsPercentageToday.hashCode() : 0);
        result = 31 * result + (uniqueDomains != null ? uniqueDomains.hashCode() : 0);
        result = 31 * result + (queriesForwarded != null ? queriesForwarded.hashCode() : 0);
        result = 31 * result + (queriesCached != null ? queriesCached.hashCode() : 0);
        result = 31 * result + (clientsEverSeen != null ? clientsEverSeen.hashCode() : 0);
        result = 31 * result + (uniqueClients != null ? uniqueClients.hashCode() : 0);
        result = 31 * result + (dnsQueriesAllTypes != null ? dnsQueriesAllTypes.hashCode() : 0);
        result = 31 * result + (replyUnknown != null ? replyUnknown.hashCode() : 0);
        result = 31 * result + (replyNoData != null ? replyNoData.hashCode() : 0);
        result = 31 * result + (replyNXDomain != null ? replyNXDomain.hashCode() : 0);
        result = 31 * result + (replyCName != null ? replyCName.hashCode() : 0);
        result = 31 * result + (replyIP != null ? replyIP.hashCode() : 0);
        result = 31 * result + (replyDomain != null ? replyDomain.hashCode() : 0);
        result = 31 * result + (replyRRName != null ? replyRRName.hashCode() : 0);
        result = 31 * result + (replyServFail != null ? replyServFail.hashCode() : 0);
        result = 31 * result + (replyRefused != null ? replyRefused.hashCode() : 0);
        result = 31 * result + (replyNotImp != null ? replyNotImp.hashCode() : 0);
        result = 31 * result + (replyOther != null ? replyOther.hashCode() : 0);
        result = 31 * result + (replyDNSSEC != null ? replyDNSSEC.hashCode() : 0);
        result = 31 * result + (replyNone != null ? replyNone.hashCode() : 0);
        result = 31 * result + (replyBlob != null ? replyBlob.hashCode() : 0);
        result = 31 * result + (dnsQueriesAllReplies != null ? dnsQueriesAllReplies.hashCode() : 0);
        result = 31 * result + (privacyLevel != null ? privacyLevel.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (gravityLastUpdated != null ? gravityLastUpdated.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DnsStatistics{" + //
                "domainsBeingBlocked=" + domainsBeingBlocked + //
                ", dnsQueriesToday=" + dnsQueriesToday + //
                ", adsBlockedToday=" + adsBlockedToday + //
                ", adsPercentageToday=" + adsPercentageToday + //
                ", uniqueDomains=" + uniqueDomains + //
                ", queriesForwarded=" + queriesForwarded + //
                ", queriesCached=" + queriesCached + //
                ", clientsEverSeen=" + clientsEverSeen + //
                ", uniqueClients=" + uniqueClients + //
                ", dnsQueriesAllTypes=" + dnsQueriesAllTypes + //
                ", replyUnknown=" + replyUnknown + //
                ", replyNoData=" + replyNoData + //
                ", replyNXDomain=" + replyNXDomain + //
                ", replyCName=" + replyCName + //
                ", replyIP=" + replyIP + //
                ", replyDomain=" + replyDomain + //
                ", replyRRName=" + replyRRName + //
                ", replyServFail=" + replyServFail + //
                ", replyRefused=" + replyRefused + //
                ", replyNotImp=" + replyNotImp + //
                ", replyOther=" + replyOther + //
                ", replyDNSSEC=" + replyDNSSEC + //
                ", replyNone=" + replyNone + //
                ", replyBlob=" + replyBlob + //
                ", dnsQueriesAllReplies=" + dnsQueriesAllReplies + //
                ", privacyLevel=" + privacyLevel + //
                ", status='" + status + '\'' + //
                ", gravityLastUpdated=" + gravityLastUpdated + //
                '}';
    }
}
