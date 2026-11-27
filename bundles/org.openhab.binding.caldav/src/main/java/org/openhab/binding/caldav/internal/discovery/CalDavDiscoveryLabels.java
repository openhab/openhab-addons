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
package org.openhab.binding.caldav.internal.discovery;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.caldav.internal.client.CalendarCollection;

/**
 * Resolves labels across a canonical, deduplicated account scan without changing unique server display names.
 *
 * @author Andreas Vilippus - Initial contribution
 */
@NonNullByDefault
final class CalDavDiscoveryLabels {
    private static final int MAX_SUFFIX_LENGTH = 80;

    private CalDavDiscoveryLabels() {
    }

    static Map<URI, String> create(Map<URI, CalendarCollection> collections) {
        Map<String, List<URI>> groups = new TreeMap<>();
        collections.forEach((uri, collection) -> Objects
                .requireNonNull(groups.computeIfAbsent(
                        collection.displayName().isBlank() ? "" : collection.displayName(), name -> new ArrayList<>()))
                .add(uri));
        Map<URI, String> labels = new HashMap<>();
        Set<String> used = new HashSet<>();
        groups.forEach((name, uris) -> {
            if (!name.isEmpty() && uris.size() == 1) {
                labels.put(uris.getFirst(), name);
                used.add(name);
            }
        });
        groups.forEach((name, uris) -> {
            if (name.isEmpty() || uris.size() > 1) {
                disambiguate(name, uris, labels, used);
            }
        });
        return labels;
    }

    private static void disambiguate(String name, List<URI> uris, Map<URI, String> labels, Set<String> used) {
        Map<URI, List<String>> suffixes = new HashMap<>();
        Map<String, Integer> counts = new HashMap<>();
        Map<URI, String> digests = new HashMap<>();
        for (URI uri : uris) {
            List<String> candidates = pathSuffixes(uri.getRawPath());
            suffixes.put(uri, candidates);
            candidates.forEach(suffix -> counts.merge(suffix, 1, Integer::sum));
            digests.put(uri, digest(uri));
        }
        for (URI uri : uris) {
            for (String suffix : Objects.requireNonNull(suffixes.get(uri))) {
                String label = label(name, suffix);
                if (counts.getOrDefault(suffix, 0) == 1 && used.add(label)) {
                    labels.put(uri, label);
                    break;
                }
            }
        }
        Map<Integer, Map<String, Integer>> prefixCounts = new HashMap<>();
        for (URI uri : uris) {
            if (labels.containsKey(uri)) {
                continue;
            }
            String digest = Objects.requireNonNull(digests.get(uri));
            for (int length = 8; length <= digest.length(); length++) {
                Map<String, Integer> prefixes = Objects.requireNonNull(prefixCounts.computeIfAbsent(length, size -> {
                    Map<String, Integer> result = new HashMap<>();
                    digests.values().forEach(value -> result.merge(value.substring(0, size), 1, Integer::sum));
                    return result;
                }));
                String prefix = digest.substring(0, length);
                String label = label(name, prefix);
                if (prefixes.getOrDefault(prefix, 0) == 1 && used.add(label)) {
                    labels.put(uri, label);
                    break;
                }
            }
            if (!labels.containsKey(uri)) {
                // A server display name can itself equal even the full digest label; keep that name unchanged.
                String base = label(name, digest);
                for (int index = 2;; index++) {
                    String label = base + " (" + index + ")";
                    if (used.add(label)) {
                        labels.put(uri, label);
                        break;
                    }
                }
            }
        }
    }

    private static List<String> pathSuffixes(String path) {
        int end = path.length();
        while (end > 0 && path.charAt(end - 1) == '/') {
            end--;
        }
        int first = 0;
        while (first < end && path.charAt(first) == '/') {
            first++;
        }
        List<String> suffixes = new ArrayList<>();
        int start = end;
        while (start > first) {
            start = path.lastIndexOf('/', start - 1) + 1;
            if (end - start > MAX_SUFFIX_LENGTH) {
                break;
            }
            String suffix = path.substring(start, end);
            if (!suffix.isBlank()) {
                suffixes.add(suffix);
            }
            start--;
        }
        return suffixes;
    }

    private static String label(String name, String suffix) {
        return name.isEmpty() ? suffix : name + " (" + suffix + ")";
    }

    static String digest(URI canonicalUri) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(canonicalUri.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
