/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.io.neeo.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.addon.AddonInfo;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.type.ThingType;
import org.openhab.io.neeo.internal.models.NeeoDevice;
import org.openhab.io.neeo.internal.models.TokenScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class emulates the same search pattern that the NEEO brain uses (https://github.com/neophob/tokensearch.js) on
 * all the exposed things in the registry.
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class TokenSearch {

    private final Logger logger = LoggerFactory.getLogger(TokenSearch.class);

    /** The service context */
    private final ServiceContext context;

    /** The search threshold */
    private final double threshold;

    /** The search limit */
    private final int searchLimit;

    /** The delimiter used to split search terms */
    private static final char DELIMITER = ' ';

    /**
     * Instantiates a new token search based on the {@link ServiceContext} and threshold
     *
     * @param context the non-null context
     * @param threshold the threshold between 0 and 1
     */
    public TokenSearch(ServiceContext context, double threshold) {
        Objects.requireNonNull(context, "context cannot be null");
        if (threshold < 0 || threshold > 1) {
            throw new IllegalArgumentException("threshold must be between 0 and 1");
        }

        this.threshold = threshold;
        this.context = context;

        final Object searchLimitText = context.getComponentContext().getProperties().get(NeeoConstants.CFG_SEARCHLIMIT);
        int searchLimit = 10;
        if (searchLimitText != null) {
            try {
                searchLimit = Integer.parseInt(searchLimitText.toString());
            } catch (NumberFormatException e) {
                logger.debug("{} was not a valid integer, defaulting to {}: {}", NeeoConstants.CFG_SEARCHLIMIT,
                        searchLimit, searchLimitText);
            }
        }
        this.searchLimit = searchLimit;
    }

    /**
     * Searches the registry for all {@link NeeoDevice} matching the query
     *
     * @param query the non-empty query
     * @return a non-null result
     */
    public Result search(String query) {
        NeeoUtil.requireNotEmpty(query, "query cannot be empty");

        final List<TokenScore<NeeoDevice>> results = new ArrayList<>();

        final String[] needles = StringUtils.split(query, DELIMITER);
        int maxScore = -1;

        for (NeeoDevice device : context.getDefinitions().getExposed()) {
            int score = search(device.getName(), needles);
            score += search("openhab", needles);
            // score += searchAlgorithm(thing.getLocation(), needles);
            score += search(device.getUid().getBindingId(), needles);

            final Thing thing = context.getThingRegistry().get(device.getUid().asThingUID());
            if (thing != null) {
                final String location = thing.getLocation();
                if (location != null && !location.isEmpty()) {
                    score += search(location, needles);
                }

                final Map<@NonNull String, String> properties = thing.getProperties();
                final String vendor = properties.get(Thing.PROPERTY_VENDOR);
                if (vendor != null && !vendor.isEmpty()) {
                    score += search(vendor, needles);
                }

                final ThingType tt = context.getThingTypeRegistry().getThingType(thing.getThingTypeUID());
                if (tt != null) {
                    score += search(tt.getLabel(), needles);

                    final AddonInfo bi = context.getAddonInfoRegistry().getAddonInfo(tt.getBindingId());
                    if (bi != null) {
                        score += search(bi.getName(), needles);
                    }
                }
            }

            maxScore = Math.max(maxScore, score);

            results.add(new TokenScore<>(score, device));
        }

        return new Result(applyThreshold(results, maxScore, threshold), maxScore);
    }

    /**
     * Search the 'haystack' for the needles. The 'haystack' will be broken up by delimiter and each part will be
     * compared to the needles array and the resulting score summation returned.
     *
     * @param haystack the search term
     * @param needles the items to search
     * @return the score of the match
     */
    private int search(String haystack, String[] needles) {
        return Arrays.stream(StringUtils.split(haystack, DELIMITER)).mapToInt(hs -> searchAlgorithm(hs, needles)).sum();
    }

    /**
     * The search algorithm (lifted from tokensearch.js)
     *
     * @param haystack the search term
     * @param needles the items to search
     * @return the score of the match
     */
    private int searchAlgorithm(String haystack, String[] needles) {
        Objects.requireNonNull(needles, "needles cannot be null");

        int score = 0;

        int arrayLength = needles.length;
        for (int i = 0; i < arrayLength; i++) {
            String needle = needles[i];
            int stringPos = haystack.toLowerCase().indexOf(needle.toLowerCase());
            int tokenScore = 0;
            if (stringPos > -1) {
                if (needle.length() < 2) {
                    tokenScore = 1;
                } else {
                    if (haystack.equalsIgnoreCase(needle)) {
                        tokenScore = 6;
                    } else if (stringPos == 0) {
                        tokenScore = 2;
                    } else {
                        tokenScore = 1;
                    }
                }
            }
            score += tokenScore;
        }
        return score;
    }

    /**
     * Apply threshold to the results (lifted from tokensearch.js)
     *
     * @param collection the collection of items
     * @param maxScore the maximum score
     * @param threshold the threshold
     * @return the list passing the threshold
     */
    private List<TokenScore<NeeoDevice>> applyThreshold(List<TokenScore<NeeoDevice>> collection, int maxScore,
            double threshold) {
        Objects.requireNonNull(collection, "collection cannot be null");

        final double normalizedScore = 1d / maxScore;
        final List<TokenScore<NeeoDevice>> results = new ArrayList<>();

        for (TokenScore<NeeoDevice> ts : collection) {
            double score = 1 - ts.getScore() * normalizedScore;
            if (score <= threshold) {
                results.add(new TokenScore<>(score, ts.getItem()));
            }
        }

        // Sort and then limit by search limit
        return results.stream().sorted().limit(searchLimit).collect(Collectors.toList());
    }

    /**
     * The results of a token search. The return list of devices will be filtered by those below the threshold and
     * limited to certain size (10 by default)
     *
     * @author Tim Roberts - initial contribution
     */
    public class Result {
        /** Maximum score found */
        private final int maxScore;

        /** Filtered list of devices */
        private final List<TokenScore<NeeoDevice>> devices;

        /**
         * Constructs the result from the devices and max score
         *
         * @param devices a non-null, potentially empty filtered list of devices
         * @param maxScore the maximum score (negative if there are no matching devices)
         */
        private Result(List<TokenScore<NeeoDevice>> devices, int maxScore) {
            Objects.requireNonNull(devices, "devices must not be null");
            this.devices = devices;
            this.maxScore = maxScore;
        }

        /**
         * The maximum score over all devices. Will be negative if there were no matches
         *
         * @return the maximum score
         */
        public int getMaxScore() {
            return maxScore;
        }

        /**
         * The list of devices that are below the threshold. The size of the arry will be limited to a certain size (10
         * by default)
         *
         * @return a non-null, possibly empty list of devices
         */
        public List<TokenScore<NeeoDevice>> getDevices() {
            return devices;
        }
    }
}
