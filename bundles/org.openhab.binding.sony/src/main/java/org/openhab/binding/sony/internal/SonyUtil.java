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
package org.openhab.binding.sony.internal;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.AbstractUID;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.sony.internal.net.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various, usually unrelated, utility functions used across the sony binding
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SonyUtil {

    /** Bigdecimal hundred (used in scale/unscale methods) */
    public static final BigDecimal BIGDECIMAL_HUNDRED = BigDecimal.valueOf(100);

    /**
     * Creates a channel identifier from the group (if specified) and channel id
     *
     * @param groupId the possibly null, possibly empty group id
     * @param channelId the non-null, non-empty channel id
     * @return a non-null, non-empty channel id
     */
    public static String createChannelId(final @Nullable String groupId, final String channelId) {
        Validate.notEmpty(channelId, "channelId cannot be empty");
        return groupId == null || StringUtils.isEmpty(groupId) ? channelId : (groupId + "#" + channelId);
    }

    /**
     * This utility function will take a potential channelUID string and return a valid channelUID by removing all
     * invalidate characters (see {@link AbstractUID#SEGMENT_PATTERN})
     *
     * @param channelUIId the non-null, possibly empty channelUID to validate
     * @return a non-null, potentially empty string representing what was valid
     */
    public static String createValidChannelUId(final String channelUID) {
        Objects.requireNonNull(channelUID, "channelUID cannot be null");
        final String id = channelUID.replaceAll("[^A-Za-z0-9_-]", "").toLowerCase();
        return StringUtils.isEmpty(id) ? "na" : id;
    }

    /**
     * Utility function to close a {@link AutoCloseable} and log any exception thrown.
     *
     * @param closeable a possibly null {@link AutoCloseable}. If null, no action is done.
     */
    public static void close(final @Nullable AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final Exception e) {
                LoggerFactory.getLogger(SonyUtil.class).debug("Exception closing: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Determines if the current thread has been interrupted or not
     *
     * @return true if interrupted, false otherwise
     */
    public static boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

    /**
     * Checks whether the current thread has been interrupted and throws {@link InterruptedException} if it's been
     * interrupted.
     *
     * @throws InterruptedException the interrupted exception
     */
    public static void checkInterrupt() throws InterruptedException {
        if (isInterrupted()) {
            throw new InterruptedException("thread interrupted");
        }
    }

    /**
     * Cancels the specified {@link Future}.
     *
     * @param future a possibly null future. If null, no action is done
     */
    public static void cancel(final @Nullable Future<?> future) {
        if (future != null) {
            future.cancel(true);
        }
    }

    /**
     * Returns a new string type or UnDefType.UNDEF if the string is null
     *
     * @param str the possibly null string
     * @return either a StringType or UnDefType.UNDEF is null
     */
    public static State newStringType(final @Nullable String str) {
        return str == null ? UnDefType.UNDEF : new StringType(str);
    }

    /**
     * Returns a new quantity type or UnDefType.UNDEF if the integer is null
     *
     * @param itgr the possibly null integer
     * @param unit a non-null unit
     * @return either a QuantityType or UnDefType.UNDEF is null
     */
    public static <T extends Quantity<T>> State newQuantityType(final @Nullable Integer itgr, final Unit<T> unit) {
        Objects.requireNonNull(unit, "unit cannot be null");
        return itgr == null ? UnDefType.UNDEF : new QuantityType<T>(itgr, unit);
    }

    /**
     * Returns a new quantity type or UnDefType.UNDEF if the double is null
     *
     * @param dbl the possibly null double
     * @param unit a non-null unit
     * @return either a QuantityType or UnDefType.UNDEF is null
     */
    public static <T extends Quantity<T>> State newQuantityType(final @Nullable Double dbl, final Unit<T> unit) {
        Objects.requireNonNull(unit, "unit cannot be null");
        return dbl == null ? UnDefType.UNDEF : new QuantityType<T>(dbl, unit);
    }

    /**
     * Returns a new decimal type or UnDefType.UNDEF if the integer is null
     *
     * @param itgr the possibly null integer
     * @return either a DecimalType or UnDefType.UNDEF is null
     */
    public static State newDecimalType(final @Nullable Integer itgr) {
        return itgr == null ? UnDefType.UNDEF : new DecimalType(itgr);
    }

    /**
     * Returns a new decimal type or UnDefType.UNDEF if the double is null
     *
     * @param dbl the possibly null double
     * @return either a DecimalType or UnDefType.UNDEF is null
     */
    public static State newDecimalType(final @Nullable Double dbl) {
        return dbl == null ? UnDefType.UNDEF : new DecimalType(dbl);
    }

    /**
     * Returns a new decimal type or UnDefType.UNDEF if the string representation is null
     *
     * @param nbr the possibly null, possibly empty string decimal
     * @return either a DecimalType or UnDefType.UNDEF is null
     */
    public static State newDecimalType(final @Nullable String nbr) {
        return nbr == null || StringUtils.isEmpty(nbr) ? UnDefType.UNDEF : new DecimalType(nbr);
    }

    /**
     * Returns a new percent type or UnDefType.UNDEF if the value is null
     *
     * @param val the possibly null big decimal
     * @return either a PercentType or UnDefType.UNDEF is null
     */
    public static State newPercentType(final @Nullable BigDecimal val) {
        return val == null ? UnDefType.UNDEF : new PercentType(val);
    }

    /**
     * Returns a new percent type or UnDefType.UNDEF if the value is null
     *
     * @param val the possibly null big decimal
     * @return either a PercentType or UnDefType.UNDEF is null
     */
    public static State newBooleanType(final @Nullable Boolean val) {
        return val == null ? UnDefType.UNDEF : val.booleanValue() ? OnOffType.ON : OnOffType.OFF;
    }

    /**
     * Scales the associated big decimal within the miniumum/maximum defined
     *
     * @param value a non-null value to scale
     * @param minimum a possibly null minimum value (if null, zero will be used)
     * @param maximum a possibly null maximum value (if null, 100 will be used)
     * @return a scaled big decimal value
     */
    public static BigDecimal scale(final BigDecimal value, final @Nullable BigDecimal minimum,
            final @Nullable BigDecimal maximum) {
        Objects.requireNonNull(value, "value cannot be null");

        final int initialScale = value.scale();

        final BigDecimal min = minimum == null ? BigDecimal.ZERO : minimum;
        final BigDecimal max = maximum == null ? BIGDECIMAL_HUNDRED : maximum;

        if (min.compareTo(max) > 0) {
            return BigDecimal.ZERO;
        }

        final BigDecimal val = guard(value, min, max);
        final BigDecimal scaled = val.subtract(min).multiply(BIGDECIMAL_HUNDRED).divide(max.subtract(min),
                initialScale + 2, RoundingMode.HALF_UP);
        return guard(scaled.setScale(initialScale, RoundingMode.HALF_UP), BigDecimal.ZERO, BIGDECIMAL_HUNDRED);
    }

    /**
     * Unscales the associated big decimal within the miniumum/maximum defined
     *
     * @param value a non-null scaled value
     * @param minimum a possibly null minimum value (if null, zero will be used)
     * @param maximum a possibly null maximum value (if null, 100 will be used)
     * @return a scaled big decimal value
     */
    public static BigDecimal unscale(final BigDecimal scaledValue, final @Nullable BigDecimal minimum,
            final @Nullable BigDecimal maximum) {
        Objects.requireNonNull(scaledValue, "scaledValue cannot be null");

        final int initialScale = scaledValue.scale();
        final BigDecimal min = minimum == null ? BigDecimal.ZERO : minimum;
        final BigDecimal max = maximum == null ? BIGDECIMAL_HUNDRED : maximum;

        if (min.compareTo(max) > 0) {
            return min;
        }

        final BigDecimal scaled = guard(scaledValue, BigDecimal.ZERO, BIGDECIMAL_HUNDRED);
        final BigDecimal val = max.subtract(min)
                .multiply(scaled.divide(BIGDECIMAL_HUNDRED, initialScale + 2, RoundingMode.HALF_UP)).add(min);

        return guard(val.setScale(initialScale, RoundingMode.HALF_UP), min, max);
    }

    /**
     * Provides a guard to value (value must be within the min/max range - if outside, will be set to the min or max)
     *
     * @param value a non-null value to guard
     * @param minimum a non-null minimum value
     * @param maximum a non-null maximum value
     * @return a big decimal within the min/max range
     */
    public static BigDecimal guard(final BigDecimal value, final BigDecimal minimum, final BigDecimal maximum) {
        Objects.requireNonNull(value, "value cannot be null");
        Objects.requireNonNull(minimum, "minimum cannot be null");
        Objects.requireNonNull(maximum, "maximum cannot be null");
        if (value.compareTo(minimum) < 0) {
            return minimum;
        }
        if (value.compareTo(maximum) > 0) {
            return maximum;
        }
        return value;
    }

    /**
     * Performs a WOL if there is a configured ip address and mac address. If either ip address or mac address is
     * null/empty, call is ignored
     * 
     * @param logger the non-null logger to log messages to
     * @param deviceIpAddress the possibly null, possibly empty device ip address
     * @param deviceMacAddress the possibly null, possibly empty device mac address
     */
    public static void sendWakeOnLan(final Logger logger, final @Nullable String deviceIpAddress,
            final @Nullable String deviceMacAddress) {
        Objects.requireNonNull(logger, "logger cannot be null");

        if (deviceIpAddress != null && deviceMacAddress != null && StringUtils.isNotBlank(deviceIpAddress)
                && StringUtils.isNotBlank(deviceMacAddress)) {
            try {
                NetUtil.sendWol(deviceIpAddress, deviceMacAddress);
                logger.debug("WOL packet sent to {}", deviceMacAddress);
            } catch (final IOException e) {
                logger.debug("Exception occurred sending WOL packet to {}", deviceMacAddress, e);
            }
        } else {
            logger.debug(
                    "WOL packet is not supported - specify the IP address and mac address in config if you want a WOL packet sent");
        }
    }

    /**
     * Returns true if the two maps are: both null or of equal size, all keys and values (case insensitve) match
     * 
     * @param map1 a possibly null, possibly empty map
     * @param map2 a possibly null, possibly empty map
     * @return true if they match, false otherwise
     */
    public static boolean equalsIgnoreCase(final Map<String, String> map1, final Map<String, String> map2) {
        Objects.requireNonNull(map1, "map1 cannot be null");
        Objects.requireNonNull(map2, "map2 cannot be null");

        if (map1.size() != map2.size()) {
            return false;
        }

        final Map<String, String> lowerMap1 = map1.entrySet().stream()
                .map(s -> new AbstractMap.SimpleEntry<>(StringUtils.lowerCase(s.getKey()),
                        StringUtils.lowerCase(s.getValue())))
                .collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue()));

        final Map<String, String> lowerMap2 = map1.entrySet().stream()
                .map(s -> new AbstractMap.SimpleEntry<>(StringUtils.lowerCase(s.getKey()),
                        StringUtils.lowerCase(s.getValue())))
                .collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue()));

        return lowerMap1.equals(lowerMap2);
    }

    /**
     * Returns true if the two sets are: both null or of equal size and all keys match (case insensitive)
     * 
     * @param set1 a possibly null, possibly empty set
     * @param set2 a possibly null, possibly empty set
     * @return true if they match, false otherwise
     */
    public static boolean equalsIgnoreCase(final @Nullable Set<@Nullable String> set1,
            final @Nullable Set<@Nullable String> set2) {
        if (set1 == null && set2 == null) {
            return true;
        }

        if (set2 == null) {
            return false;
        }

        if (set1.size() != set2.size()) {
            return false;
        }

        final TreeSet<@Nullable String> tset1 = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        tset1.addAll(set1);
        final TreeSet<@Nullable String> tset2 = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        tset2.addAll(set2);
        return tset1.equals(tset2);
    }

    /**
     * Determines if the model name is valid (alphanumeric plus dash)
     * 
     * @param modelName a non-null, possibly empty model name
     * @return true if a valid model name, false otherwise
     */
    public static boolean isValidModelName(final String modelName) {
        return modelName.matches("[A-Za-z0-9-]+");// && modelName.matches(".*\\d\\d.*"); - only valid for tvs
    }

    /**
     * Determines if the thing type UID is a generic thing type (scalar) or a custom one (scalar-X800)
     * 
     * @param uid a non-null UID
     * @return true if generic, false otherwise
     */
    public static boolean isGenericThingType(final ThingTypeUID uid) {
        Objects.requireNonNull(uid, "uid cannot be null");

        final String typeId = uid.getId();
        return typeId.indexOf("-") < 0;
    }

    /**
     * Get's the service name from a thing type uid ("scalar" for example if "scalar" or "scalar-X800" or
     * "scalar-X800_V2")
     * 
     * @param uid a non-null UID
     * @return a non-null service name
     */
    public static String getServiceName(final ThingTypeUID uid) {
        Objects.requireNonNull(uid, "uid cannot be null");

        final String typeId = uid.getId();
        final int idx = typeId.indexOf("-");

        return idx < 0 ? typeId : typeId.substring(0, idx);
    }

    /**
     * Get's the model name from a thing type uid (null if just "scalar" or "X800" if "scalar-X800" or "X800" if
     * "scalar-X800_V2")
     * 
     * @param uid a non-null UID
     * @return a model name or null if not found (ie generic)
     */
    public static @Nullable String getModelName(final ThingTypeUID uid) {
        Objects.requireNonNull(uid, "uid cannot be null");

        final String typeId = uid.getId();
        final int idx = typeId.indexOf("-");
        if (idx < 0) {
            return null;
        }

        final String modelName = typeId.substring(idx + 1);

        final int versIdx = modelName.lastIndexOf(SonyBindingConstants.MODELNAME_VERSION_PREFIX);
        return versIdx >= 0 ? modelName.substring(0, versIdx) : modelName;
    }

    /**
     * Get's the model version number from a thing type uid ("2" if "scalar-X800_V2" or 0 in all other cases)
     * 
     * @param uid a non-null thing type uid
     * @return the model version (with 0 being the default)
     */
    public static int getModelVersion(final ThingTypeUID uid) {
        Objects.requireNonNull(uid, "uid cannot be null");

        final String modelName = getModelName(uid);
        if (modelName == null || StringUtils.isEmpty(modelName)) {
            return 0;
        }

        final int versIdx = modelName.lastIndexOf(SonyBindingConstants.MODELNAME_VERSION_PREFIX);
        if (versIdx > 0) {
            final String vers = modelName.substring(versIdx + SonyBindingConstants.MODELNAME_VERSION_PREFIX.length());
            try {
                return Integer.parseInt(vers);
            } catch (final NumberFormatException e) {
                return 0;
            }
        }

        return 0;
    }

    /**
     * Determins if a thing type service/model name (which can contain wildcards) matches the corresponding
     * service/model name (regardless of the model version)
     * 
     * @param thingTypeServiceName a possibly null, possibly empty thing service name
     * @param thingTypeModelName a possibly null, possibly empty thing model name. Use "x" (case sensitive) to denote a
     *            wildcard (like 'XBR-xX830' to match all screen sizes)
     * @param serviceName a non-null, non-empty service name
     * @param modelName a non-null, non-empty model name
     * @return true if they match (regardless of model name version), false otherwise
     */
    public static boolean isModelMatch(final @Nullable String thingTypeServiceName,
            final @Nullable String thingTypeModelName, final String serviceName, final String modelName) {
        Validate.notEmpty(serviceName, "serviceName cannot be empty");
        Validate.notEmpty(modelName, "modelName cannot be empty");
        if (thingTypeServiceName == null || StringUtils.isEmpty(thingTypeServiceName)) {
            return false;
        }

        if (thingTypeModelName == null || StringUtils.isEmpty(thingTypeModelName)) {
            return false;
        }

        String modelPattern = thingTypeModelName.replaceAll("x", ".*").toLowerCase();

        // remove a version identifier ("_V1" or "_V292")
        final int versIdx = modelPattern.lastIndexOf(SonyBindingConstants.MODELNAME_VERSION_PREFIX.toLowerCase());
        if (versIdx > 0) {
            final String vers = modelPattern.substring(versIdx + 2);
            if (StringUtils.isNumeric(vers)) {
                modelPattern = modelPattern.substring(0, versIdx);
            }
        }

        return StringUtils.equals(thingTypeServiceName, serviceName) && modelName.toLowerCase().matches(modelPattern);
    }

    /**
     * Determines if the thingtype uid matches the specified serviceName/model name
     * 
     * @param uid a non-null thing type UID
     * @param serviceName a non-null, non-empty service name
     * @param modelName a non-null, non-empty model name
     * @return true if they match (regardless of model name version), false otherwise
     */
    public static boolean isModelMatch(final ThingTypeUID uid, final String serviceName, final String modelName) {
        Objects.requireNonNull(uid, "uid cannot be null");
        Validate.notEmpty(modelName, "modelName cannot be empty");

        final String uidServiceName = getServiceName(uid);
        final String uidModelName = getModelName(uid);
        return uidModelName == null || StringUtils.isEmpty(uidModelName) ? false
                : isModelMatch(uidServiceName, uidModelName, serviceName, modelName);
    }

    /**
     * Converts a nullable list (with nullable elements) to a non-null list (containing no null elements) by filtering
     * all null elements out
     * 
     * @param list the list to convert
     * @return a non-null list of the same type
     */
    public static <T> List<T> convertNull(final @Nullable List<@Nullable T> list) {
        if (list == null) {
            return new ArrayList<>();
        }

        return list.stream().filter(e -> e != null).collect(Collectors.toList());
    }

    /**
     * Converts a nullable array (with nullable elements) to a non-null list (containing no null elements) by filtering
     * all null elements out
     * 
     * @param list the array to convert
     * @return a non-null list of the same type
     */
    public static <T> List<T> convertNull(final @Nullable T @Nullable [] list) {
        if (list == null) {
            return new ArrayList<>();
        }

        return Arrays.stream(list).filter(e -> e != null).collect(Collectors.toList());
    }

    /**
     * Determines if the pass class is a primitive (we treat string as a primitive here)
     * 
     * @param clazz a non-null class
     * @return true if primitive, false otherwise
     */
    public static <T> boolean isPrimitive(final Class<T> clazz) {
        Objects.requireNonNull(clazz, "clazz cannot be null");
        return clazz.isPrimitive() || ClassUtils.wrapperToPrimitive(clazz) != null || clazz == String.class;
    }
}
