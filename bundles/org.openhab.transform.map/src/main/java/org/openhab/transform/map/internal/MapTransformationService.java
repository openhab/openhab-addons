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

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.transform.AbstractFileTransformationService;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The implementation of {@link TransformationService} which simply maps strings to other strings
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Gaël L'hopital - Make it localizable
 */
@NonNullByDefault
@Component(service = { TransformationService.class, ConfigOptionProvider.class }, property = {
        "openhab.transform=MAP" })
public class MapTransformationService extends AbstractFileTransformationService<Properties>
        implements ConfigOptionProvider {

    private final Logger logger = LoggerFactory.getLogger(MapTransformationService.class);

    private static final String PROFILE_CONFIG_URI = "profile:transform:MAP";
    private static final String CONFIG_PARAM_FUNCTION = "function";
    private static final String[] FILE_NAME_EXTENSIONS = { "map" };

    /**
     * <p>
     * Transforms the input <code>source</code> by mapping it to another string. It expects the mappings to be read from
     * a file which is stored under the 'configurations/transform' folder. This file should be in property syntax, i.e.
     * simple lines with "key=value" pairs. To organize the various transformations one might use subfolders.
     *
     * @param properties the list of properties which contains the key value pairs for the mapping.
     * @param source the input to transform
     * @return the transformed result or null if the transformation couldn't be completed for any reason.
     */
    @Override
    protected @Nullable String internalTransform(Properties properties, String source) throws TransformationException {
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

    @Override
    protected Properties internalLoadTransform(String filename) throws TransformationException {
        Properties result = new Properties();
        try (FileReader reader = new FileReader(filename)) {
            result.load(reader);
            return result;
        } catch (IOException e) {
            throw new TransformationException("An error occurred while opening file.", e);
        }
    }

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable String context,
            @Nullable Locale locale) {
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
