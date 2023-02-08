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
package org.openhab.transform.map.internal;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
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
 * <p>
 * The implementation of {@link TransformationService} which simply maps strings to other strings
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Gaël L'hopital - Make it localizable
 * @author Jan N. Klug - Refactored to use {@link TransformationRegistry}
 */
@NonNullByDefault
@Component(service = { TransformationService.class, ConfigOptionProvider.class }, property = {
        "openhab.transform=MAP" })
public class MapTransformationService
        implements TransformationService, ConfigOptionProvider, RegistryChangeListener<Transformation> {
    private static final String SOURCE_VALUE = "_source_";
    private static final String PROFILE_CONFIG_URI = "profile:transform:MAP";
    private static final String CONFIG_PARAM_FUNCTION = "function";
    private static final Set<String> SUPPORTED_CONFIGURATION_TYPES = Set.of("map");

    private final Logger logger = LoggerFactory.getLogger(MapTransformationService.class);
    private final TransformationRegistry transformationRegistry;
    private final Map<String, Properties> cachedTransformations = new ConcurrentHashMap<>();

    @Activate
    public MapTransformationService(@Reference TransformationRegistry transformationRegistry) {
        this.transformationRegistry = transformationRegistry;
        transformationRegistry.addRegistryChangeListener(this);
    }

    @Deactivate
    public void deactivate() {
        transformationRegistry.removeRegistryChangeListener(this);
    }

    @Override
    public @Nullable String transform(String function, String source) throws TransformationException {
        // always get a configuration from the registry to account for changed system locale
        Transformation transformation = transformationRegistry.get(function, null);

        if (transformation != null) {
            if (!cachedTransformations.containsKey(transformation.getUID())) {
                importConfiguration(transformation);
            }
            Properties properties = cachedTransformations.get(function);
            if (properties != null) {
                String target = properties.getProperty(source);

                if (target == null) {
                    target = properties.getProperty("");
                    if (target == null) {
                        throw new TransformationException("Target value not found in map for '" + source + "'");
                    } else if (SOURCE_VALUE.equals(target)) {
                        target = source;
                    }
                }

                logger.debug("Transformation resulted in '{}'", target);
                return target;
            }
        }
        throw new TransformationException("Could not find configuration '" + function + "' or failed to parse it.");
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

    private void importConfiguration(@Nullable Transformation transformation) {
        if (transformation != null) {
            try {
                Properties properties = new Properties();
                String function = transformation.getConfiguration().get(Transformation.FUNCTION);
                if (function == null || function.isBlank()) {
                    logger.warn("Function not defined for transformation '{}'", transformation.getUID());
                    return;
                }
                properties.load(new StringReader(function));
                cachedTransformations.put(transformation.getUID(), properties);
            } catch (IOException ignored) {
            }
        }
    }
}
