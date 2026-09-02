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
package org.openhab.io.mcp.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;

/**
 * Scores how well an item's name, label, and synonyms match a free-text query.
 * Combines substring phrase matching with Jaro-Winkler token similarity to tolerate
 * typos ("kichen" → "kitchen") and word reordering ("light kitchen" → "kitchen light").
 *
 * Synonyms come from the {@code synonyms} metadata namespace, a comma-separated list
 * per item — the openHAB-native way to add alternative names.
 * 
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class FuzzyItemMatcher {

    private static final String SYNONYMS_NAMESPACE = "synonyms";
    private static final String TOKEN_SPLIT = "[\\s'\\-_,;:/]+";
    private static final double PHRASE_WEIGHT = 0.6;
    private static final double TOKEN_WEIGHT = 0.4;
    private static final double JW_PREFIX_WEIGHT = 0.1;

    private final @Nullable MetadataRegistry metadataRegistry;

    public FuzzyItemMatcher(@Nullable MetadataRegistry metadataRegistry) {
        this.metadataRegistry = metadataRegistry;
    }

    /**
     * Score in [0.0, 1.0]. Higher is a better match. A blank query returns 1.0
     * (everything matches equally).
     */
    public double score(Item item, String query) {
        String q = query.toLowerCase(Locale.ROOT).trim();
        if (q.isEmpty()) {
            return 1.0;
        }
        List<String> haystack = buildHaystack(item);
        String[] qTokens = q.split(TOKEN_SPLIT);

        double bestPhrase = 0.0;
        double bestTokenAvg = 0.0;
        for (String entry : haystack) {
            bestPhrase = Math.max(bestPhrase, jw(q, entry));
            bestTokenAvg = Math.max(bestTokenAvg, tokenAverage(qTokens, entry));
        }
        return PHRASE_WEIGHT * bestPhrase + TOKEN_WEIGHT * bestTokenAvg;
    }

    /**
     * Returns the lowercased strings we match against for this item: the name, the
     * label (if set), and any {@code synonyms} metadata entries.
     */
    private List<String> buildHaystack(Item item) {
        List<String> entries = new ArrayList<>(3);
        entries.add(item.getName().toLowerCase(Locale.ROOT));
        String label = item.getLabel();
        if (label != null && !label.isBlank()) {
            entries.add(label.toLowerCase(Locale.ROOT));
        }
        MetadataRegistry registry = metadataRegistry;
        if (registry != null) {
            Metadata md = registry.get(new MetadataKey(SYNONYMS_NAMESPACE, item.getName()));
            if (md != null) {
                for (String syn : md.getValue().split(",")) {
                    String trimmed = syn.trim();
                    if (!trimmed.isEmpty()) {
                        entries.add(trimmed.toLowerCase(Locale.ROOT));
                    }
                }
            }
        }
        return entries;
    }

    private static double tokenAverage(String[] queryTokens, String entry) {
        String[] entryTokens = entry.split(TOKEN_SPLIT);
        if (queryTokens.length == 0 || entryTokens.length == 0) {
            return 0.0;
        }
        double total = 0.0;
        for (String qt : queryTokens) {
            double best = 0.0;
            for (String et : entryTokens) {
                best = Math.max(best, jw(qt, et));
            }
            total += best;
        }
        return total / queryTokens.length;
    }

    private static double jw(String a, String b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        double jaro = jaro(a, b);
        int prefix = 0;
        int limit = Math.min(4, Math.min(a.length(), b.length()));
        while (prefix < limit && a.charAt(prefix) == b.charAt(prefix)) {
            prefix++;
        }
        return jaro + prefix * JW_PREFIX_WEIGHT * (1.0 - jaro);
    }

    private static double jaro(String s, String t) {
        int sLen = s.length();
        int tLen = t.length();
        int matchWindow = Math.max(sLen, tLen) / 2 - 1;
        if (matchWindow < 0) {
            matchWindow = 0;
        }
        boolean[] sMatched = new boolean[sLen];
        boolean[] tMatched = new boolean[tLen];
        int matches = 0;
        int transpositions = 0;
        for (int i = 0; i < sLen; i++) {
            int start = Math.max(0, i - matchWindow);
            int end = Math.min(i + matchWindow + 1, tLen);
            for (int j = start; j < end; j++) {
                if (!tMatched[j] && s.charAt(i) == t.charAt(j)) {
                    sMatched[i] = true;
                    tMatched[j] = true;
                    matches++;
                    break;
                }
            }
        }
        if (matches == 0) {
            return 0.0;
        }
        int k = 0;
        for (int i = 0; i < sLen; i++) {
            if (!sMatched[i]) {
                continue;
            }
            while (!tMatched[k]) {
                k++;
            }
            if (s.charAt(i) != t.charAt(k)) {
                transpositions++;
            }
            k++;
        }
        return ((double) matches / sLen + (double) matches / tLen + (matches - transpositions / 2.0) / matches) / 3.0;
    }
}
