/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.transform.bin2json.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of {@link TransformationService} which transforms the
 * hexa string formatted binary data by Binary Block Parser syntax to JSON format.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
@Component(property = { "openhab.transform=BIN2JSON" })
public class Bin2JsonTransformationService implements TransformationService {

    private Logger logger = LoggerFactory.getLogger(Bin2JsonTransformationService.class);

    /**
     * Transforms the input <code>source</code> by Java Binary Block Parser syntax.
     *
     * @param syntax Java Binary Block Parser syntax.
     * @param source the input to transform
     */
    @Override
    public @Nullable String transform(String syntax, String source) throws TransformationException {
        final long startTime = System.currentTimeMillis();
        logger.debug("About to transform '{}' by the Bin2Json syntax '{}'", source, syntax);

        String result = "";

        try {
            result = String.valueOf(new Bin2Json(syntax).convert(source));
            logger.debug("transformation resulted '{}'", result);
            return result;
        } catch (ConversionException e) {
            throw new TransformationException("An error occurred while executing the converter. " + e.getMessage(), e);
        } finally {
            logger.trace("Bin2Json execution elapsed {} ms. Result: {}", System.currentTimeMillis() - startTime,
                    result);
        }
    }
}
