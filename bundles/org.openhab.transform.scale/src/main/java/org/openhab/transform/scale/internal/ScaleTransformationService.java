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
package org.openhab.transform.scale.internal;

import java.io.IOException;
import java.io.StringReader;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.transform.Transformation;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationRegistry;
import org.openhab.core.transform.TransformationService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
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
@NonNullByDefault
public class ScaleTransformationService
        implements TransformationService, ConfigOptionProvider, RegistryChangeListener<Transformation> {

    private final Logger logger = LoggerFactory.getLogger(ScaleTransformationService.class);

    private static final String PROFILE_CONFIG_URI = "profile:transform:SCALE";
    private static final String CONFIG_PARAM_FUNCTION = "function";
    private static final Set<String> SUPPORTED_CONFIGURATION_TYPES = Set.of("scale");

    /** RegEx to extract a scale definition */
    private static final Pattern LIMITS_PATTERN = Pattern.compile("(\\[|])(.*)\\.\\.(.*)(\\[|])");

    private static final String NON_NUMBER = "NaN";
    private static final String FORMAT = "format";
    private static final String FORMAT_VALUE = "%value%";
    private static final String FORMAT_LABEL = "%label%";

    /** Inaccessible range used to store presentation format ]0..0[ */
    private static final Range FORMAT_RANGE = Range.range(BigDecimal.ZERO, false, BigDecimal.ZERO, false);
    private final TransformationRegistry transformationRegistry;

    private final Map<String, Map<@Nullable Range, String>> cachedTransformations = new ConcurrentHashMap<>();

    @Activate
    public ScaleTransformationService(@Reference TransformationRegistry transformationRegistry) {
        this.transformationRegistry = transformationRegistry;
        transformationRegistry.addRegistryChangeListener(this);
    }

    @Deactivate
    public void deactivate() {
        transformationRegistry.removeRegistryChangeListener(this);
    }

    @Override
    public void added(Transformation element) {
        // do nothing, configurations are added to cache if needed
    }

    @Override
    public void removed(Transformation element) {
        cachedTransformations.remove(element.getUID());
    }

    @Override
    public void updated(Transformation oldElement, Transformation element) {
        if (cachedTransformations.remove(oldElement.getUID()) != null) {
            // import only if it was present before
            importConfiguration(element);
        }
    }

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
        private final HashSet<@Nullable Object> keys = new LinkedHashSet<>();

        Set<@Nullable Object> orderedKeys() {
            return keys;
        }

        @Override
        public @NonNullByDefault({}) Enumeration<Object> keys() {
            return Collections.enumeration(keys);
        }

        @Override
        public @Nullable Object put(@Nullable Object key, @Nullable Object value) {
            keys.add(key);
            return super.put(key, value);
        }
    }

    @Override
    public @Nullable String transform(String function, String source) throws TransformationException {
        // always get a configuration from the registry to account for changed system locale
        Transformation transformation = transformationRegistry.get(function, null);

        if (transformation != null) {
            if (!cachedTransformations.containsKey(transformation.getUID())) {
                importConfiguration(transformation);
            }
            Map<@Nullable Range, String> data = cachedTransformations.get(transformation.getUID());

            if (data != null) {
                String target;

                try {
                    final BigDecimal value = new BigDecimal(source);
                    target = formatResult(data, source, value);
                } catch (NumberFormatException e) {
                    // Scale can only be used with numeric inputs, so lets try to see if ever its a valid quantity type
                    try {
                        final QuantityType<?> quantity = new QuantityType<>(source);
                        return formatResult(data, source, quantity.toBigDecimal());
                    } catch (IllegalArgumentException e2) {
                        String nonNumeric = data.get(null);
                        if (nonNumeric != null) {
                            target = nonNumeric;
                        } else {
                            throw new TransformationException(
                                    "Scale must be used with numeric inputs, valid quantity types or a 'NaN' entry.");
                        }
                    }
                }
                logger.debug("Transformation resulted in '{}'", target);
                return target;
            }
        }

        throw new TransformationException("Could not find configuration '" + function + "' or failed to parse it.");
    }

    private String formatResult(Map<@Nullable Range, String> data, String source, final BigDecimal value)
            throws TransformationException {
        String format = data.get(FORMAT_RANGE);
        String result = getScaleResult(data, source, value);
        return format.replaceAll(FORMAT_VALUE, source).replaceAll(FORMAT_LABEL, result);
    }

    private String getScaleResult(Map<@Nullable Range, String> data, String source, final BigDecimal value)
            throws TransformationException {
        return data.entrySet().stream().filter(entry -> entry.getKey() != null && entry.getKey().contains(value))
                .findFirst().map(Map.Entry::getValue)
                .orElseThrow(() -> new TransformationException("No matching range for '" + source + "'"));
    }

    private void importConfiguration(@Nullable Transformation configuration) {
        if (configuration != null) {
            try {
                final Map<@Nullable Range, String> data = new LinkedHashMap<>();
                data.put(FORMAT_RANGE, FORMAT_LABEL);
                final OrderedProperties properties = new OrderedProperties();
                String function = configuration.getConfiguration().get(Transformation.FUNCTION);
                if (function == null) {
                    return;
                }
                properties.load(new StringReader(function));

                for (Object orderedKey : properties.orderedKeys()) {
                    final String entry = (String) orderedKey;
                    final String value = properties.getProperty(entry);
                    final Matcher matcher = LIMITS_PATTERN.matcher(entry);
                    if (matcher.matches() && (matcher.groupCount() == 4)) {
                        final boolean lowerInclusive = matcher.group(1).equals("[");
                        final boolean upperInclusive = matcher.group(4).equals("]");

                        final String lowLimit = matcher.group(2);
                        final String highLimit = matcher.group(3);

                        final BigDecimal lowValue = lowLimit.isEmpty() ? null : new BigDecimal(lowLimit);
                        final BigDecimal highValue = highLimit.isEmpty() ? null : new BigDecimal(highLimit);
                        final Range range = Range.range(lowValue, lowerInclusive, highValue, upperInclusive);

                        data.put(range, value);
                    } else {
                        if (NON_NUMBER.equals(entry)) {
                            data.put(null, value);
                        } else if (FORMAT.equals(entry)) {
                            data.put(FORMAT_RANGE, value);
                        } else {
                            logger.warn(
                                    "Scale transformation configuration '{}' does not comply with syntax for entry : '{}', '{}'",
                                    configuration.getUID(), entry, value);
                        }
                    }
                }

                cachedTransformations.put(configuration.getUID(), data);
            } catch (IOException | NumberFormatException ignored) {
            }
        }
    }

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable String context,
            @Nullable Locale locale) {
        if (PROFILE_CONFIG_URI.equals(uri.toString())) {
            if (CONFIG_PARAM_FUNCTION.equals(param)) {
                return transformationRegistry.getTransformations(SUPPORTED_CONFIGURATION_TYPES).stream()
                        .map(c -> new ParameterOption(c.getUID(), c.getLabel())).collect(Collectors.toList());
            }
        }
        return null;
    }
}
