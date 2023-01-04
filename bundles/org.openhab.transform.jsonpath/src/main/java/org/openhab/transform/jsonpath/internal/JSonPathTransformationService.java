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
package org.openhab.transform.jsonpath.internal;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.UnDefType;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

/**
 * <p>
 * The implementation of {@link TransformationService} which transforms the input by JSonPath Expressions.
 *
 * @author Gaël L'hopital
 * @author Sebastian Janzen
 *
 */
@NonNullByDefault
@Component(property = { "openhab.transform=JSONPATH" })
public class JSonPathTransformationService implements TransformationService {

    private final Logger logger = LoggerFactory.getLogger(JSonPathTransformationService.class);

    /**
     * Transforms the input <code>source</code> by JSonPath expression.
     *
     * @param function JsonPath expression
     * @param source String which contains JSON
     * @throws TransformationException If the JsonPath expression is invalid, an {@link InvalidPathException} is thrown,
     *             which is encapsulated in a {@link TransformationException}.
     */
    @Override
    public @Nullable String transform(String jsonPathExpression, String source) throws TransformationException {
        if (jsonPathExpression == null || source == null) {
            throw new TransformationException("the given parameters 'JSonPath' and 'source' must not be null");
        }

        logger.debug("about to transform '{}' by the function '{}'", source, jsonPathExpression);

        try {
            Object transformationResult = JsonPath.read(source, jsonPathExpression);
            logger.debug("transformation resulted in '{}'", transformationResult);
            if (transformationResult == null) {
                return null;
            } else if (transformationResult instanceof List) {
                return flattenList((List<?>) transformationResult);
            } else {
                return transformationResult.toString();
            }
        } catch (PathNotFoundException e) {
            throw new TransformationException("Invalid path '" + jsonPathExpression + "' in '" + source + "'");
        } catch (InvalidPathException | InvalidJsonException e) {
            throw new TransformationException("An error occurred while transforming JSON expression.", e);
        }
    }

    private String flattenList(List<?> list) {
        if (list.size() == 1) {
            return list.get(0).toString();
        }
        if (list.size() > 1) {
            if (list.get(0) instanceof Number || list.get(0) instanceof Boolean) {
                return createNumberList(list);
            } else if (list.get(0) instanceof String) {
                return createStringList(list);
            }
            logger.warn(
                    "JsonPath expressions with more than one result are only supported for Boolean, Number and String data types, please adapt your selector. Result: {}",
                    list);
        }
        return UnDefType.NULL.toFullString();
    }

    private String createNumberList(List<?> list) {
        return list.stream().map(n -> String.valueOf(n)).collect(Collectors.joining(", ", "[", "]"));
    }

    private String createStringList(List<?> list) {
        return list.stream().map(n -> "\"" + String.valueOf(n) + "\"").collect(Collectors.joining(", ", "[", "]"));
    }
}
