/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.transform.math.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for {@link TransformationService}s which applies bitwise operations on the input
 *
 * @author Christoph Weitkamp - Initial contribution
 * @author Jan N. Klug - Adapted for bit operazions
 */
@NonNullByDefault
abstract class AbstractBitwiseTransformationService implements TransformationService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Pattern NUMBER_PATTERN = Pattern.compile(".*?(-?((0)|([1-9][0-9]*))(\\.[0-9]*)?).*?");
    private static final Pattern HEX_PATTERN = Pattern.compile("\\s*0x([A-Fa-f0-9]+)\\s*");
    private static final Pattern BINARY_PATTERN = Pattern.compile("\\s*0b([01]+)\\s*");

    @Override
    public @Nullable String transform(String maskString, String sourceString) throws TransformationException {
        long source = getLongValue(sourceString);
        long mask = getLongValue(maskString);
        return Long.toString(performCalculation(source, mask));
    }

    private long getLongValue(String str) throws TransformationException {
        try {
            Matcher matcher = HEX_PATTERN.matcher(str);
            if (matcher.matches()) {
                return Long.parseLong(matcher.group(1), 16);
            } else {
                matcher = BINARY_PATTERN.matcher(str);
                if (matcher.matches()) {
                    return Long.parseLong(matcher.group(1), 2);
                } else {
                    matcher = NUMBER_PATTERN.matcher(str);
                    if (matcher.matches()) {
                        return Long.parseLong(matcher.group(1));
                    } else {
                        throw new TransformationException("Math transformation can only be used with numeric inputs");
                    }
                }
            }
        } catch (NumberFormatException e) {
            logger.warn("Input value '{}' could not converted to a valid number", str);
            throw new TransformationException("Math transformation can only be used with numeric inputs");
        }
    }

    abstract long performCalculation(long source, long mask);
}
