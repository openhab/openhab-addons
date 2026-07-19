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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents either a service or service instance name in the DNS.
 *
 * <p>
 * The special thing this class can do is (attempt) to handle periods in the instance
 * name correctly.
 *
 * @param instance instance part of the name, or {@code null} for a plain service name
 * @param service service part, e.g. {@code _http._tcp}
 * @param domain domain part, e.g. {@code local}
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record ServiceInstanceName(@Nullable String instance, String service, String domain) {

    /**
     * Splits a name into instance (optional), service, and domain parts.
     *
     * <p>
     * The service label is found by looking for a label starting with {@code _}
     * immediately followed by {@code _tcp} or {@code _udp} (compared case-insensitively,
     * the only case-insensitive name compare needed by the scanner).
     *
     * @param name full DNS name
     * @return the split name
     * @throws IllegalArgumentException if the name is not a service (instance) name
     */
    public static ServiceInstanceName splitName(String name) {
        String[] labels = name.split("\\.", -1);
        if (labels.length < 2) {
            throw new IllegalArgumentException("There must be at least three labels in a service name");
        }
        for (int index = 0; index < labels.length - 1; index++) {
            String label = labels[index];
            String nextLabel = labels[index + 1].toLowerCase(Locale.ROOT);
            if (label.startsWith("_") && ("_tcp".equals(nextLabel) || "_udp".equals(nextLabel))) {
                String instance = String.join(".", java.util.Arrays.asList(labels).subList(0, index));
                String domain = String.join(".", java.util.Arrays.asList(labels).subList(index + 2, labels.length));
                return new ServiceInstanceName(instance.isEmpty() ? null : instance, label + "." + labels[index + 1],
                        domain);
            }
        }
        throw new IllegalArgumentException("'" + name + "' is not a service domain, nor a service instance name");
    }

    /**
     * Returns just the service name, like the name for a PTR record.
     *
     * @return service name joined with the domain
     */
    public String ptrName() {
        return service + "." + domain;
    }

    /**
     * Returns the labels of this name (instance first when present), used when encoding
     * a QNAME so that dots inside the instance label survive.
     *
     * @return the labels
     */
    public List<String> labels() {
        List<String> labels = new ArrayList<>();
        String instanceLabel = instance;
        if (instanceLabel != null) {
            labels.add(instanceLabel);
        }
        for (String label : ptrName().split("\\.", -1)) {
            labels.add(label);
        }
        return labels;
    }

    @Override
    public String toString() {
        StringBuilder joined = new StringBuilder();
        for (@Nullable
        String part : new @Nullable String[] { instance, service, domain }) {
            if (part != null && !part.isEmpty()) {
                if (joined.length() > 0) {
                    joined.append('.');
                }
                joined.append(part);
            }
        }
        return joined.toString();
    }
}
