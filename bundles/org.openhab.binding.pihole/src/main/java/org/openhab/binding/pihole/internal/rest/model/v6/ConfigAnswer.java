/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.pihole.internal.rest.model.v6;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public record ConfigAnswer(Config config, double took) {

    public record Temp(int limit, String unit) {
    }

    public record Api(int maxSessions, boolean prettyJSON, String pwhash, String password, String totpSecret,
            String appPwhash, boolean appSudo, boolean cliPw, List<Object> excludeClients, List<Object> excludeDomains,
            int maxHistory, int maxClients, boolean clientHistoryGlobalMax, boolean allowDestructive, Temp temp) {
    }

    public record Host(boolean force4, String iPv4, boolean force6, String iPv6) {
    }

    public record Blocking(boolean active, String mode, String edns) {
    }

    public record Blocking_1(boolean force4, String iPv4, boolean force6, String iPv6) {
    }

    public record Cache(int size, int optimizer, int upstreamBlockedTTL) {
    }

    public record Network(boolean parseARPcache, int expire) {
    }

    public record Database(boolean dBimport, int maxDBdays, int dBinterval, boolean useWAL, Network network) {
    }

    public record Debug(boolean database, boolean networking, boolean locks, boolean queries, boolean flags,
            boolean shmem, boolean gc, boolean arp, boolean regex, boolean api, boolean tls, boolean overtime,
            boolean status, boolean caps, boolean dnssec, boolean vectors, boolean resolver, boolean edns0,
            boolean clients, boolean aliasclients, boolean events, boolean helper, boolean config, boolean inotify,
            boolean webserver, boolean extra, boolean reserved, boolean ntp, boolean netlink, boolean all) {
    }

    public record Dhcp(boolean active, String start, String end, String router, String netmask, String leaseTime,
            boolean ipv6, boolean rapidCommit, boolean multiDNS, boolean logging, boolean ignoreUnknownClients,
            List<String> hosts) {
    }

    public record SpecialDomains(boolean mozillaCanary, boolean iCloudPrivateRelay, boolean designatedResolver) {
    }

    public record Reply(Host host, Blocking_1 blocking) {
    }

    public record RateLimit(int count, int interval) {
    }

    public record Domain(String name, boolean local) {

    }

    public record Dns(List<String> upstreams, boolean cNAMEdeepInspect, boolean blockESNI, boolean edns0ecs,
            boolean ignoreLocalhost, boolean showDNSSEC, boolean analyzeOnlyAandAAAA, String piholePTR,
            String replyWhenBusy, int blockTTL, List<String> hosts, boolean domainNeeded, boolean expandHosts,
            Domain domain, boolean bogusPriv, boolean dnssec, String _interface, String hostRecord,
            String listeningMode, boolean queryLogging, List<Object> cnameRecords, int port, List<Object> revServers,
            Cache cache, Blocking blocking, SpecialDomains specialDomains, Reply reply, RateLimit rateLimit) {
    }

    public record Files(String pid, String database, @Nullable String gravity, String gravityTmp, String macvendor,
            String pcap, Log log) {
    }

    public record Interface(boolean boxed, String theme) {
    }

    public record Ipv4(boolean active, String address) {
    }

    public record Ipv6(boolean active, String address) {
    }

    public record Log(String ftl, String dnsmasq, String webserver) {
    }

    public record Check(boolean load, int shmem, int disk) {
    }

    public record Misc(int privacylevel, int delayStartup, int nice, boolean addr2line, boolean etcDnsmasqD,
            List<Object> dnsmasqLines, boolean extraLogging, boolean readOnly, Check check) {
    }

    public record Rtc(boolean set, String device, boolean utc) {
    }

    public record Sync(boolean active, String server, int interval, int count, Rtc rtc) {
    }

    public record Ntp(Ipv4 ipv4, Ipv6 ipv6, Sync sync) {
    }

    public record Paths(String webroot, String webhome, String prefix) {
    }

    public record Resolver(boolean resolveIPv4, boolean resolveIPv6, boolean networkNames, String refreshNames) {
    }

    public record Session(int timeout, boolean restore) {
    }

    public record Tls(String cert) {
    }

    public record Webserver(String domain, String acl, String port, int threads, List<String> headers, boolean serveAll,
            Session session, Tls tls, Paths paths, Interface _interface, Api api) {
    }

    public record Config(Dns dns, Dhcp dhcp, Ntp ntp, Resolver resolver, Database database, Webserver webserver,
            Files files, Misc misc, Debug debug) {
    }
}
