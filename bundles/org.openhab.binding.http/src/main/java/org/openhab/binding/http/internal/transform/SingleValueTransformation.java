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
package org.openhab.binding.http.internal.transform;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A transformation for a value used in {@link HttpChannel}.
 *
 * @author David Graeff - Initial contribution
 * @author Jan N. Klug - adapted from MQTT binding to HTTP binding
 */
@NonNullByDefault
public class SingleValueTransformation implements ValueTransformation {
    private final Logger logger = LoggerFactory.getLogger(SingleValueTransformation.class);
    private final Function<String, @Nullable TransformationService> transformationServiceSupplier;
    private WeakReference<@Nullable TransformationService> transformationService = new WeakReference<>(null);
    private final String pattern;
    private final String serviceName;

    /**
     * Creates a new channel state transformer.
     *
     * @param pattern A transformation pattern, starting with the transformation service
     *            name, followed by a colon and the transformation itself.
     * @param transformationServiceSupplier
     */
    public SingleValueTransformation(String pattern,
            Function<String, @Nullable TransformationService> transformationServiceSupplier) {
        this.transformationServiceSupplier = transformationServiceSupplier;
        int index = pattern.indexOf(':');
        if (index == -1) {
            throw new IllegalArgumentException(
                    "The transformation pattern must consist of the type and the pattern separated by a colon");
        }
        this.serviceName = pattern.substring(0, index).toUpperCase();
        this.pattern = pattern.substring(index + 1);
    }

    @Override
    public Optional<String> apply(String value) {
        TransformationService transformationService = this.transformationService.get();
        if (transformationService == null) {
            transformationService = transformationServiceSupplier.apply(serviceName);
            if (transformationService == null) {
                logger.warn("Transformation service {} for pattern {} not found!", serviceName, pattern);
                return Optional.empty();
            }
            this.transformationService = new WeakReference<>(transformationService);
        }

        try {
            String result = transformationService.transform(pattern, value);
            if (result == null) {
                logger.debug("Transformation {} returned empty result when applied to {}.", this, value);
                return Optional.empty();
            }
            return Optional.of(result);
        } catch (TransformationException e) {
            logger.warn("Executing transformation {} failed: {}", this, e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public String toString() {
        return "ChannelStateTransformation{pattern='" + pattern + "', serviceName='" + serviceName + "'}";
    }
}
