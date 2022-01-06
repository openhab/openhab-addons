/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.transform.scale.internal;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.transform.AbstractFileTransformationService;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of {@link TransformationService} which transforms the
 * input by matching it between limits of ranges in a scale file
 *
 * @author Gaël L'hopital
 * @author Markus Rathgeb - drop usage of Guava
 */
@Component(service = { TransformationService.class, ConfigOptionProvider.class }, property = {
        "openhab.transform=SCALE" })
public class ScaleTransformationService extends AbstractFileTransformationService<Map<Range, String>>
        implements ConfigOptionProvider {

    private final Logger logger = LoggerFactory.getLogger(ScaleTransformationService.class);

    private static final String PROFILE_CONFIG_URI = "profile:transform:SCALE";
    private static final String CONFIG_PARAM_FUNCTION = "function";
    private static final String[] FILE_NAME_EXTENSIONS = { "scale" };

    /** RegEx to extract a scale definition */
    private static final Pattern LIMITS_PATTERN = Pattern.compile("(\\[|\\])(.*)\\.\\.(.*)(\\[|\\])");

    private static final String NON_NUMBER = "NaN";
    private static final String FORMAT = "format";
    private static final String FORMAT_VALUE = "%value%";
    private static final String FORMAT_LABEL = "%label%";

    /** Inaccessible range used to store presentation format ]0..0[ */
    private static final Range FORMAT_RANGE = Range.range(BigDecimal.ZERO, false, BigDecimal.ZERO, false);

    /**
     * The implementation of {@link OrderedProperties} that let access
     * properties in the same order than presented in the source file
     * by using the orderedKeys function.
     *
     * This implementation is limited to the sole purpose of the class
     * (e.g. it does not handle removing elements)
     *
     * @author Gaël L'hopital
     */
    static class OrderedProperties extends Properties {
        private static final long serialVersionUID = 3860553217028220119L;
        private final HashSet<Object> keys = new LinkedHashSet<>();

        Set<Object> orderedKeys() {
            return keys;
        }

        @Override
        public Enumeration<Object> keys() {
            return Collections.<Object> enumeration(keys);
        }

        @Override
        public Object put(Object key, Object value) {
            keys.add(key);
            return super.put(key, value);
        }
    }

    /**
     * Performs transformation of the input <code>source</code>
     *
     * The method transforms the input <code>source</code> by matching searching
     * the range where it fits i.e. [min..max]=value or ]min..max]=value
     *
     * @param properties the list of properties defining all the available ranges
     * @param source the input to transform
     * @return the transformed result or null if the transformation couldn't be completed for any reason.
     */
    @Override
    protected @Nullable String internalTransform(Map<Range, String> data, String source)
            throws TransformationException {
        try {
            final BigDecimal value = new BigDecimal(source);
            return formatResult(data, source, value);
        } catch (NumberFormatException e) {
            // Scale can only be used with numeric inputs, so lets try to see if ever its a valid quantity type
            try {
                final QuantityType<?> quantity = new QuantityType<>(source);
                return formatResult(data, source, quantity.toBigDecimal());
            } catch (IllegalArgumentException e2) {
                String nonNumeric = data.get(null);
                if (nonNumeric != null) {
                    return nonNumeric;
                } else {
                    throw new TransformationException(
                            "Scale must be used with numeric inputs, valid quantity types or a 'NaN' entry.");
                }
            }
        }
    }

    private String formatResult(Map<Range, String> data, String source, final BigDecimal value)
            throws TransformationException {
        String format = data.get(FORMAT_RANGE);
        String result = getScaleResult(data, source, value);
        return format.replaceAll(FORMAT_VALUE, source).replaceAll(FORMAT_LABEL, result);
    }

    private String getScaleResult(Map<Range, String> data, String source, final BigDecimal value)
            throws TransformationException {
        return data.entrySet().stream().filter(entry -> entry.getKey() != null && entry.getKey().contains(value))
                .findFirst().map(Map.Entry::getValue)
                .orElseThrow(() -> new TransformationException("No matching range for '" + source + "'"));
    }

    @Override
    protected Map<Range, String> internalLoadTransform(String filename) throws TransformationException {
        try (FileReader reader = new FileReader(filename)) {
            final Map<Range, String> data = new LinkedHashMap<>();
            data.put(FORMAT_RANGE, FORMAT_LABEL);
            final OrderedProperties properties = new OrderedProperties();
            properties.load(reader);

            for (Object orderedKey : properties.orderedKeys()) {
                final String entry = (String) orderedKey;
                final String value = properties.getProperty(entry);
                final Matcher matcher = LIMITS_PATTERN.matcher(entry);
                if (matcher.matches() && (matcher.groupCount() == 4)) {
                    final boolean lowerInclusive = matcher.group(1).equals("[");
                    final boolean upperInclusive = matcher.group(4).equals("]");

                    final String lowLimit = matcher.group(2);
                    final String highLimit = matcher.group(3);

                    try {
                        final BigDecimal lowValue = lowLimit.isEmpty() ? null : new BigDecimal(lowLimit);
                        final BigDecimal highValue = highLimit.isEmpty() ? null : new BigDecimal(highLimit);
                        final Range range = Range.range(lowValue, lowerInclusive, highValue, upperInclusive);

                        data.put(range, value);
                    } catch (NumberFormatException ex) {
                        throw new TransformationException("Error parsing bounds: " + lowLimit + ".." + highLimit);
                    }
                } else {
                    if (NON_NUMBER.equals(entry)) {
                        data.put(null, value);
                    } else if (FORMAT.equals(entry)) {
                        data.put(FORMAT_RANGE, value);
                    } else {
                        logger.warn("Scale transform file '{}' does not comply with syntax for entry : '{}', '{}'",
                                filename, entry, value);
                    }
                }
            }

            return data;
        } catch (final IOException ex) {
            throw new TransformationException("An error occurred while opening file.", ex);
        }
    }

    @Override
    public @Nullable Collection<@NonNull ParameterOption> getParameterOptions(URI uri, String param,
            @Nullable String context, @Nullable Locale locale) {
        if (PROFILE_CONFIG_URI.equals(uri.toString())) {
            switch (param) {
                case CONFIG_PARAM_FUNCTION:
                    return getFilenames(FILE_NAME_EXTENSIONS).stream().map(f -> new ParameterOption(f, f))
                            .collect(Collectors.toList());
            }
        }
        return null;
    }
}
