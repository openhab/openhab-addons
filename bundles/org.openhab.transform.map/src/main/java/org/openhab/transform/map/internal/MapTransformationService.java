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
package org.openhab.transform.map.internal;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.smarthome.core.transform.AbstractFileTransformationService;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
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
@Component(immediate = true, service = TransformationService.class, property = { "smarthome.transform=MAP" })
public class MapTransformationService extends AbstractFileTransformationService<Properties> {

    private final Logger logger = LoggerFactory.getLogger(MapTransformationService.class);

    /**
     * <p>
     * Transforms the input <code>source</code> by mapping it to another string. It expects the mappings to be read from
     * a file which is stored under the 'configurations/transform' folder. This file should be in property syntax, i.e.
     * simple lines with "key=value" pairs. To organize the various transformations one might use subfolders.
     *
     * @param properties the list of properties which contains the key value pairs for the mapping.
     * @param source the input to transform
     */
    @Override
    protected String internalTransform(Properties properties, String source) throws TransformationException {
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
}
