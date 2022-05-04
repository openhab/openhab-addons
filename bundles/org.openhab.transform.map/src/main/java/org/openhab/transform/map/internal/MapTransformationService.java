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
import org.openhab.core.transform.TransformationConfiguration;
import org.openhab.core.transform.TransformationConfigurationRegistry;
import org.openhab.core.transform.TransformationException;
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
 * @author Jan N. Klug - Refactored to use {@link TransformationConfigurationRegistry}
 */
@NonNullByDefault
@Component(service = { TransformationService.class, ConfigOptionProvider.class }, property = {
        "openhab.transform=MAP" })
public class MapTransformationService
        implements TransformationService, ConfigOptionProvider, RegistryChangeListener<TransformationConfiguration> {
    private final Logger logger = LoggerFactory.getLogger(MapTransformationService.class);

    private static final String PROFILE_CONFIG_URI = "profile:transform:MAP";
    private static final String CONFIG_PARAM_FUNCTION = "function";
    private static final Set<String> SUPPORTED_CONFIGURATION_TYPES = Set.of("map");

    private final TransformationConfigurationRegistry transformationConfigurationRegistry;
    private final Map<String, Properties> cachedTransformations = new ConcurrentHashMap<>();

    @Activate
    public MapTransformationService(
            @Reference TransformationConfigurationRegistry transformationConfigurationRegistry) {
        this.transformationConfigurationRegistry = transformationConfigurationRegistry;
        transformationConfigurationRegistry.addRegistryChangeListener(this);
    }

    @Deactivate
    public void deactivate() {
        transformationConfigurationRegistry.removeRegistryChangeListener(this);
    }

    @Override
    public @Nullable String transform(String function, String source) throws TransformationException {
        // always get a configuration from the registry to account for changed system locale
        TransformationConfiguration transformationConfiguration = transformationConfigurationRegistry.get(function,
                null);

        if (transformationConfiguration != null) {
            if (!cachedTransformations.containsKey(transformationConfiguration.getUID())) {
                importConfiguration(transformationConfiguration);
            }
            Properties properties = cachedTransformations.get(function);
            if (properties != null) {
                String target = properties.getProperty(source);

                if (target == null) {
                    target = properties.getProperty("");
                    if (target == null) {
                        throw new TransformationException("Target value not found in map for '" + source + "'");
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
                return transformationConfigurationRegistry.getConfigurations(SUPPORTED_CONFIGURATION_TYPES).stream()
                        .map(c -> new ParameterOption(c.getUID(), c.getLabel())).collect(Collectors.toList());
            }
        }
        return null;
    }

    @Override
    public void added(TransformationConfiguration element) {
        // do nothing, configurations are added to cache if needed
    }

    @Override
    public void removed(TransformationConfiguration element) {
        cachedTransformations.remove(element.getUID());
    }

    @Override
    public void updated(TransformationConfiguration oldElement, TransformationConfiguration element) {
        if (cachedTransformations.remove(oldElement.getUID()) != null) {
            // import only if it was present before
            importConfiguration(element);
        }
    }

    private void importConfiguration(@Nullable TransformationConfiguration configuration) {
        if (configuration != null) {
            try {
                Properties properties = new Properties();
                properties.load(new StringReader(configuration.getContent()));
                cachedTransformations.put(configuration.getUID(), properties);
            } catch (IOException ignored) {
            }
        }
    }
}
