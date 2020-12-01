/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.providers.sources;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.SonyUtil;
import org.openhab.binding.sony.internal.providers.models.SonyDeviceCapability;
import org.openhab.binding.sony.internal.providers.models.SonyServiceCapability;
import org.openhab.binding.sony.internal.providers.models.SonyThingChannelDefinition;
import org.openhab.binding.sony.internal.providers.models.SonyThingDefinition;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;

/**
 * This utility class provides the ability to 'match' items. A match is less stringent than an equals and allows
 * this class to define what is included or excluded from a match. Generally device properties (ie not user settable)
 * and additive properties (like something new in an array or list) are what is checked.
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
class SonyMatcher {
    /** A comparator to use to compare thing channel definitions by channel id */
    private static final Comparator<SonyThingChannelDefinition> THINGCHANNELCOMPARATOR = Comparator
            .comparing((final SonyThingChannelDefinition e) -> e.getChannelId());

    /** A comparator that can be used to compare service capabilities by serviceName then by version */
    private static final Comparator<SonyServiceCapability> SERVICECAPABILITYCOMPARATOR = Comparator
            .comparing((final SonyServiceCapability e) -> e.getServiceName()).thenComparing(e -> e.getVersion());

    /** A general matcher for {@link ScalarWebMethod} */
    private static final MatchCallback<ScalarWebMethod> METHODCALLBACK = new MatchCallback<ScalarWebMethod>() {
        @Override
        public boolean isIgnored(ScalarWebMethod item) {
            return false;
        }

        @Override
        public boolean isMatch(ScalarWebMethod left, ScalarWebMethod right) {
            return matches(left, right);
        }
    };

    /** A general matcher for {@link SonyServiceCapability} */
    private static final MatchCallback<SonyServiceCapability> SERVICECALLBACK = new MatchCallback<SonyServiceCapability>() {
        @Override
        public boolean isIgnored(SonyServiceCapability item) {
            return false;
        }

        @Override
        public boolean isMatch(SonyServiceCapability left, SonyServiceCapability right) {
            return matches(left, right);
        }
    };

    /**
     * Match two {@link SonyThingDefinition} and returns true if they match
     * 
     * @param left a non-null left
     * @param right a non-null right
     * @param meta a non-null meta
     * @return true if matched, false otherwise
     */
    public static boolean matches(final SonyThingDefinition left, final SonyThingDefinition right, MetaInfo meta) {
        Objects.requireNonNull(left, "left cannot be null");
        Objects.requireNonNull(right, "right cannot be null");
        Objects.requireNonNull(meta, "meta cannot be null");

        return StringUtils.equalsIgnoreCase(left.getService(), right.getService())
                && StringUtils.equalsIgnoreCase(left.getModelName(), right.getModelName())
                && SonyUtil.equalsIgnoreCase(left.getChannelGroups(), right.getChannelGroups())
                && matches(left.getChannels(), right.getChannels(), THINGCHANNELCOMPARATOR,
                        new MatchCallback<SonyThingChannelDefinition>() {
                            @Override
                            public boolean isIgnored(SonyThingChannelDefinition item) {
                                final String channelId = item.getChannelId();
                                return channelId != null && meta.isIgnoredChannelId(channelId);
                            }

                            @Override
                            public boolean isMatch(SonyThingChannelDefinition left, SonyThingChannelDefinition right) {
                                return matches(left, right, meta);
                            }
                        }, true);
    }

    /**
     * Match two {@link SonyThingChannelDefinition} and returns true if they match
     * 
     * @param left a non-null left
     * @param right a non-null right
     * @param meta a non-null meta
     * @return true if matched, false otherwise
     */
    private static boolean matches(final SonyThingChannelDefinition left, final SonyThingChannelDefinition right,
            MetaInfo meta) {
        Objects.requireNonNull(left, "left cannot be null");
        Objects.requireNonNull(right, "right cannot be null");
        Objects.requireNonNull(meta, "meta cannot be null");

        final String leftChannelId = left.getChannelId();
        if (leftChannelId == null || StringUtils.isEmpty(leftChannelId)) {
            return false;
        }
        final String rightChannelId = right.getChannelId();
        if (rightChannelId == null || StringUtils.isEmpty(rightChannelId)) {
            return false;
        }

        return StringUtils.equalsIgnoreCase(meta.getChannelId(leftChannelId), meta.getChannelId(rightChannelId))
                && StringUtils.equalsIgnoreCase(left.getChannelType(), right.getChannelType())
                && SonyUtil.equalsIgnoreCase(convertNull(left.getProperties()), convertNull(right.getProperties()));
    }

    /**
     * Match two {@link SonyServiceCapability} and returns true if they match
     * 
     * @param left a non-null left
     * @param right a non-null right
     * @return true if matched, false otherwise
     */
    public static boolean matches(final SonyServiceCapability left, final SonyServiceCapability right) {
        Objects.requireNonNull(left, "left cannot be null");
        Objects.requireNonNull(right, "right cannot be null");

        return StringUtils.equalsIgnoreCase(left.getServiceName(), right.getServiceName())
                && StringUtils.equalsIgnoreCase(left.getVersion(), right.getVersion())
                && StringUtils.equalsIgnoreCase(left.getTransport(), right.getTransport())
                && matches(
                        left.getMethods().stream().filter(e -> e.getVariation() != ScalarWebMethod.UNKNOWN_VARIATION)
                                .collect(Collectors.toList()),
                        right.getMethods().stream().filter(e -> e.getVariation() != ScalarWebMethod.UNKNOWN_VARIATION)
                                .collect(Collectors.toList()),
                        ScalarWebMethod.COMPARATOR, METHODCALLBACK, true)
                && matches(
                        left.getNotifications().stream()
                                .filter(e -> e.getVariation() != ScalarWebMethod.UNKNOWN_VARIATION)
                                .collect(Collectors.toList()),
                        right.getNotifications().stream()
                                .filter(e -> e.getVariation() != ScalarWebMethod.UNKNOWN_VARIATION)
                                .collect(Collectors.toList()),
                        ScalarWebMethod.COMPARATOR, METHODCALLBACK, true);
    }

    /**
     * Match two {@link SonyDeviceCapability} and returns true if they match
     * 
     * @param left a non-null left
     * @param right a non-null right
     * @return true if matched, false otherwise
     */
    public static boolean matches(final SonyDeviceCapability left, final SonyDeviceCapability right) {
        Objects.requireNonNull(left, "left cannot be null");
        Objects.requireNonNull(right, "right cannot be null");

        return StringUtils.equalsIgnoreCase(left.getModelName(), right.getModelName())
                && matches(left.getServices(), right.getServices(), SERVICECAPABILITYCOMPARATOR, SERVICECALLBACK, true);
    }

    /**
     * Match two {@link ScalarWebMethod} and returns true if they match
     * 
     * @param left a non-null left
     * @param right a non-null right
     * @return true if matched, false otherwise
     */
    public static boolean matches(final ScalarWebMethod left, final ScalarWebMethod right) {
        Objects.requireNonNull(left, "left cannot be null");
        Objects.requireNonNull(right, "right cannot be null");

        return StringUtils.equalsIgnoreCase(left.getMethodName(), right.getMethodName())
                && StringUtils.equalsIgnoreCase(left.getVersion(), right.getVersion())
                && SonyUtil.equalsIgnoreCase(new HashSet<>(left.getParms()), new HashSet<>(right.getParms()))
                && SonyUtil.equalsIgnoreCase(new HashSet<>(left.getRetVals()), new HashSet<>(right.getRetVals()));
    }

    /**
     * Determines if two lists of {@link SonyMatcher} objects are a match. A match will be true if list1 has all
     * the elements in list2. ignoreDelete will determine if the list2 having more elements than list1 is a match or not
     * 
     * @param list1 a non-null first list
     * @param list2 a non-null second list to compare against
     * @param comparator a non-null comparator to use for ordering the lists
     * @param callback a non-null callback to determine if they match
     * @param ignoreDeleted true if list1 matches list2 regardless if list2 has more elements, false if they need to be
     *            exact matches
     * @return true if matched, false otherwise
     */
    private static <T> boolean matches(final List<T> list1, final List<T> list2, final Comparator<T> comparator,
            final MatchCallback<T> callback, boolean ignoreDeleted) {
        Objects.requireNonNull(list1, "list1 cannot be null");
        Objects.requireNonNull(list2, "list2 cannot be null");
        Objects.requireNonNull(comparator, "comparator cannot be null");
        Objects.requireNonNull(callback, "callback cannot be null");

        final List<T> e1 = list1.stream().filter(e -> e != null).sorted(comparator).collect(Collectors.toList());
        final List<T> e2 = list2.stream().filter(e -> e != null).sorted(comparator).collect(Collectors.toList());

        // Special case - bother are empty so we return true!
        if (e1.isEmpty() && e2.isEmpty()) {
            return true;
        }

        // If there are no elements in list1 but elements in list2, they are all 'deleted' and
        // return how the ignoreDeleted switch is
        if (e1.isEmpty() && !e2.isEmpty()) {
            return ignoreDeleted;
        }

        // If elements in list1 but none in list2, the all are 'inserted' - return false
        if (!e1.isEmpty() && e2.isEmpty()) {
            return false;
        }

        // At this point, we are insured atleast one record in both lists...
        // loop through list1 and try to find a match in list2
        // if found, then we have a match - continue on
        // if not found, the record is 'inserted' - return false
        // if, during finding, we skip over a list2 element *and* !ignoreDeleted - return false
        int i1 = 0, i2 = 0;
        for (; i1 < e1.size(); i1++) {
            final T o1 = e1.get(i1);
            if (callback.isIgnored(o1)) {
                continue;
            }

            while (!callback.isMatch(o1, e2.get(i2++))) {
                // We ran out of elements - must be new - return false;
                if (i2 >= e2.size()) {
                    return false;
                }

                // We skipped a list2 element (deleted) - if we aren't
                // ignoring deleted - return false
                if (!ignoreDeleted) {
                    return false;
                }
            }
        }

        // At this point, we matched every element in list1 and either matched every element in
        // list2 or skipped over deleted ones (because ignoreDelete was true).
        // So return true!
        return true;
    }

    /**
     * Helper function to convert a possibly null key/value map to a non-null key value map
     * 
     * @param map a non-null, possibly empty map containing nullable key/values
     * @return a non-null, possibly empty map of non-nullable key/values
     */
    private static Map<String, String> convertNull(Map<@Nullable String, @Nullable String> map) {
        return map.entrySet().stream().map(e -> {
            final String key = e.getKey();
            final String value = e.getValue();
            return StringUtils.isEmpty(key) || StringUtils.isEmpty(value) ? null
                    : new AbstractMap.SimpleEntry<>(key, value);
        }).filter(e -> e != null).collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue()));
    }

    /**
     * Functional interface to determine if something is a match
     * 
     * @param <T> the type
     */
    @NonNullByDefault
    interface MatchCallback<T> {
        /**
         * Returns true if the item should be ignored
         * 
         * @param item a non-null item
         * @return true to be ignored, false otherwise
         */
        boolean isIgnored(T item);

        /**
         * Returns true if the two items are a match
         * 
         * @param left a non-null left
         * @param right a non-null right
         * @return true if they match, false otherwise
         */
        boolean isMatch(T left, T right);
    }
}
