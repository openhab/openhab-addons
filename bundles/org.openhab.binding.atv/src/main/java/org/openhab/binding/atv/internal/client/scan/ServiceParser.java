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
package org.openhab.binding.atv.internal.client.scan;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.support.CaseInsensitiveMap;

/**
 * Parses zeroconf services from records in DNS messages.
 *
 * <p>
 * Records from multiple messages accumulate in a table keyed by record name, so a
 * service can be assembled from PTR/SRV/TXT/A records spread over several responses.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ServiceParser {

    private final Map<String, Map<Integer, List<DnsResource>>> table = new LinkedHashMap<>();
    private final Map<String, String> ptrs = new LinkedHashMap<>(); // qname -> real name
    private @Nullable List<MdnsService> cache;

    /**
     * Adds a message with records to parse.
     *
     * @param message the DNS message
     * @return this parser, for chaining
     */
    public ServiceParser addMessage(DnsMessage message) {
        cache = null;

        List<DnsResource> records = new ArrayList<>(message.answers());
        records.addAll(message.resources());
        for (DnsResource record : records) {
            if (record.qtype() == QueryType.PTR.value() && record.qname().startsWith("_")) {
                ptrs.put(record.qname(), (String) record.rd());
            } else {
                Map<Integer, List<DnsResource>> entry = table.computeIfAbsent(record.qname(),
                        k -> new LinkedHashMap<>());
                List<DnsResource> sameType = entry.computeIfAbsent(record.qtype(), k -> new ArrayList<>());
                if (!sameType.contains(record)) {
                    sameType.add(record);
                }
            }
        }
        return this;
    }

    /**
     * Parses the accumulated records and returns services.
     *
     * @return the discovered services
     */
    public List<MdnsService> parse() {
        List<MdnsService> cached = cache;
        if (cached != null) {
            return cached;
        }

        Map<String, MdnsService> results = new LinkedHashMap<>();

        // Build services
        for (Map.Entry<String, Map<Integer, List<DnsResource>>> deviceEntry : table.entrySet()) {
            String service = deviceEntry.getKey();
            Map<Integer, List<DnsResource>> device = deviceEntry.getValue();

            ServiceInstanceName serviceName;
            try {
                serviceName = ServiceInstanceName.splitName(service);
            } catch (IllegalArgumentException e) {
                continue;
            }

            @Nullable
            SrvRecord srvRd = (SrvRecord) firstRd(QueryType.SRV, device);
            @Nullable
            String target = srvRd != null ? srvRd.target() : null;

            List<DnsResource> targetRecords = table.getOrDefault(target, Map.of()).getOrDefault(QueryType.A.value(),
                    List.of());
            @Nullable
            Inet4Address address = null;

            // Pick one address that is not link-local
            for (DnsResource record : targetRecords) {
                Inet4Address addr = parseIpv4((String) record.rd());
                if (addr != null && !addr.isLinkLocalAddress()) {
                    address = addr;
                    break;
                }
            }

            @SuppressWarnings("unchecked")
            @Nullable
            Map<String, byte[]> txt = (Map<String, byte[]>) firstRd(QueryType.TXT, device);

            @Nullable
            String instance = serviceName.instance();
            results.put(service, new MdnsService(serviceName.ptrName(), instance == null ? "" : instance, address,
                    srvRd != null ? srvRd.port() : 0, Mdns.decodeProperties(txt == null ? Map.of() : txt)));
        }

        // If there are PTRs to unknown services, create placeholders
        for (Map.Entry<String, String> ptr : ptrs.entrySet()) {
            String realName = ptr.getValue();
            if (!results.containsKey(realName)) {
                results.put(realName, new MdnsService(ptr.getKey(), realName.split("\\.", -1)[0], null, 0,
                        new CaseInsensitiveMap<>()));
            }
        }
        List<MdnsService> result = new ArrayList<>(results.values());
        cache = result;
        return result;
    }

    /** Package-private view of the accumulated record table, for test inspection. */
    Map<String, Map<Integer, List<DnsResource>>> table() {
        return table;
    }

    private static @Nullable Object firstRd(QueryType qtype, Map<Integer, List<DnsResource>> entries) {
        List<DnsResource> records = entries.get(qtype.value());
        return records == null || records.isEmpty() ? null : records.get(0).rd();
    }

    private static @Nullable Inet4Address parseIpv4(String address) {
        try {
            // The string is always a dotted quad produced by QueryType.parseRdata, so
            // no name resolution happens here.
            return (Inet4Address) InetAddress.getByName(address);
        } catch (UnknownHostException | ClassCastException e) {
            return null;
        }
    }
}
