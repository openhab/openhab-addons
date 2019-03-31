/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.net.internal.transformation;

import java.lang.ref.WeakReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A transformation utility class.
 *
 * @author David Graeff - Initial contribution
 * @author Pauli Anttila - Adapted to net binding
 */
@NonNullByDefault
public class DataTransformation {
    private final Logger logger = LoggerFactory.getLogger(DataTransformation.class);
    private final TransformationServiceProvider provider;
    private WeakReference<@Nullable TransformationService> transformationService = new WeakReference<>(null);
    final String pattern;
    final String serviceName;

    /**
     * Constructor.
     *
     * @param pattern
     * @param provider
     */
    public DataTransformation(String pattern, TransformationServiceProvider provider) {
        this.provider = provider;
        int index = pattern.indexOf(':');
        if (index == -1) {
            throw new IllegalArgumentException(
                    "The transformation pattern must consist of the type and the pattern separated by a colon");
        }
        String type = pattern.substring(0, index).toUpperCase();
        this.pattern = pattern.substring(index + 1);
        this.serviceName = type;
    }

    /**
     * .
     *
     * @param value The incoming value
     * @return The transformed value
     */
    public String processValue(String value) {
        TransformationService transformationService = this.transformationService.get();
        if (transformationService == null) {
            transformationService = provider.getTransformationService(serviceName);
            if (transformationService == null) {
                logger.warn("Transformation service {} for pattern {} not found!", serviceName, pattern);
                return value;
            }
            this.transformationService = new WeakReference<>(transformationService);
        }
        String temp = null;
        try {
            temp = transformationService.transform(pattern, value);
        } catch (TransformationException e) {
            logger.warn("Executing the {}-transformation failed: {}", serviceName, e.getMessage());
        }
        return (temp != null) ? temp : value;
    }
}
