/*
 * Copyright (c) 2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationHelper;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.osgi.framework.BundleContext;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class describing a parsed transformation function.
 *
 * @author Brian J. Tarricone
 */
@NonNullByDefault
public class Transform {
    private static final Pattern EXTRACT_FUNCTION_PATTERN = Pattern.compile("(.*?)\\((.*)\\)");

    /**
     * Parses a transformation string and ensures the sfunction specified is supported.
     *
     * @param bundleContext a {@link BundleContext} used to access the transformation service
     * @param s a string representing the transformation function and pattern
     * @return a new {@link Transform}
     * @throws IllegalArgumentException if the provided string is invalid or if the transformation service is unavailable
     */
    static Transform parse(final BundleContext bundleContext, final String s) throws IllegalArgumentException {
        final Matcher matcher = EXTRACT_FUNCTION_PATTERN.matcher(s);
        if (!matcher.matches() || !matcher.find()) {
            throw new IllegalArgumentException("Supplied string (" + s + ") is not a valid transformation funcion");
        } else {
            final String function = matcher.group(1);
            final String pattern = matcher.group(2);
            final Optional<TransformationService> service = Optional.ofNullable(TransformationHelper.getTransformationService(bundleContext, function));
            if (!service.isPresent()) {
                throw new IllegalArgumentException("Cannot get transformation service for function '" + function + "'");
            } else {
                return new Transform(service.get(), pattern);
            }
        }
    }

    private final TransformationService service;
    private final String pattern;

    private Transform(final TransformationService service, final String pattern) {
        this.service = service;
        this.pattern = pattern;
    }

    /**
     * Applies this transform to the supplied value.
     *
     * @param value the value to transform
     * @return the transformed value
     * @throws IllegalArgumentException if an error occurs during the transformation
     */
    public String applyTransform(final String value) throws IllegalArgumentException {
        try {
            return service.transform(this.pattern, value);
        } catch (final TransformationException e) {
            throw new IllegalArgumentException("Failed to transform value: " + e.getMessage(), e);
        }
    }
}
