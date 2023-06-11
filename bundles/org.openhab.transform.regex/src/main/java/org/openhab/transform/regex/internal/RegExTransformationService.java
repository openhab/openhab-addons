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
package org.openhab.transform.regex.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The implementation of {@link TransformationService} which transforms the input by Regular Expressions.
 *
 * <p>
 * <b>Note:</b> the given Regular Expression must contain exactly one group!
 *
 * @author Thomas.Eichstaedt-Engelen
 */
@NonNullByDefault
@Component(property = { "openhab.transform=REGEX" })
public class RegExTransformationService implements TransformationService {

    private final Logger logger = LoggerFactory.getLogger(RegExTransformationService.class);

    private static final Pattern SUBSTR_PATTERN = Pattern.compile("^s/(.*?[^\\\\])/(.*?[^\\\\])/(.*)$");

    @Override
    public @Nullable String transform(String regExpression, String source) throws TransformationException {
        if (regExpression == null || source == null) {
            throw new TransformationException("the given parameters 'regex' and 'source' must not be null");
        }

        logger.debug("about to transform '{}' by the function '{}'", source, regExpression);

        String result = "";

        Matcher substMatcher = SUBSTR_PATTERN.matcher(regExpression);
        if (substMatcher.matches()) {
            logger.debug("Using substitution form of regex transformation");
            String regex = substMatcher.group(1);
            String substitution = substMatcher.group(2);
            String options = substMatcher.group(3);
            if (options.equals("g")) {
                result = source.trim().replaceAll(regex, substitution);
            } else {
                result = source.trim().replaceFirst(regex, substitution);
            }
            if (result != null) {
                return result;
            }
        }

        Matcher matcher = Pattern.compile("^" + regExpression + "$", Pattern.DOTALL).matcher(source.trim());
        if (!matcher.matches()) {
            logger.debug(
                    "the given regex '^{}$' doesn't match the given content '{}' -> couldn't compute transformation",
                    regExpression, source);
            return null;
        }
        matcher.reset();

        while (matcher.find()) {
            if (matcher.groupCount() == 0) {
                logger.info(
                        "the given regular expression '^{}$' doesn't contain a group. No content will be extracted and returned!",
                        regExpression);
                continue;
            }

            result = matcher.group(1);

            if (matcher.groupCount() > 1) {
                logger.debug(
                        "the given regular expression '^{}$' contains more than one group. Only the first group will be returned!",
                        regExpression);
            }
        }

        return result;
    }
}
