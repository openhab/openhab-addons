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
package org.openhab.binding.mqtt.generic;

import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A transformation for a {@link ChannelState}. It is applied for each received value on an MQTT topic.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ChannelStateTransformation {
    private final Logger logger = LoggerFactory.getLogger(ChannelStateTransformation.class);
    private final TransformationServiceProvider provider;
    private WeakReference<@Nullable TransformationService> transformationService = new WeakReference<>(null);
    final String pattern;
    final String serviceName;
    private final Pattern transformationPattern = Pattern
            .compile("(?<service1>[a-zA-Z]+):(?<pattern1>.*)|(?<service2>[a-zA-Z]+)\\((?<pattern2>.*)\\)$");

    /**
     * Creates a new channel state transformer.
     *
     * @param pattern A transformation pattern, starting with the transformation service
     *            name,followed by a colon and the transformation itself. An Example:
     *            JSONPATH:$.device.status.temperature for a json {device: {status: {
     *            temperature: 23.2 }}}.
     * @param provider The transformation service provider
     */
    public ChannelStateTransformation(String pattern, TransformationServiceProvider provider) {
        this.provider = provider;
        Matcher matcher = transformationPattern.matcher(pattern);
        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    "The transformation pattern must be in the syntax of TYPE:PATTERN or TYPE(PATTERN)");
        }
        String service = matcher.group("service2");
        if (service == null) {
            this.serviceName = matcher.group("service1").toUpperCase();
            this.pattern = matcher.group("pattern1");
        } else {
            this.serviceName = service.toUpperCase();
            this.pattern = matcher.group("pattern2");
        }
    }

    /**
     * Creates a new channel state transformer.
     *
     * @param serviceName A transformation service name.
     * @param pattern A transformation. An Example:
     *            $.device.status.temperature for a json {device: {status: {
     *            temperature: 23.2 }}} (for type <code>JSONPATH</code>).
     * @param provider The transformation service provider
     */
    public ChannelStateTransformation(String serviceName, String pattern, TransformationServiceProvider provider) {
        this.serviceName = serviceName;
        this.pattern = pattern;
        this.provider = provider;
    }

    /**
     * Will be called by the {@link ChannelState} for each incoming MQTT value.
     *
     * @param value The incoming value
     * @return The transformed value
     */
    protected @Nullable String processValue(String value) {
        TransformationService transformationService = this.transformationService.get();
        if (transformationService == null) {
            transformationService = provider.getTransformationService(serviceName);
            if (transformationService == null) {
                logger.warn("Transformation service {} for pattern {} not found!", serviceName, pattern);
                return value;
            }
            this.transformationService = new WeakReference<>(transformationService);
        }
        String returnValue = null;
        try {
            returnValue = transformationService.transform(pattern, value);
        } catch (TransformationException e) {
            logger.warn("Executing the {}-transformation failed: {}. Pattern: '{}'. Value: '{}'", serviceName,
                    e.getMessage(), pattern, value);
        }
        return returnValue;
    }
}
