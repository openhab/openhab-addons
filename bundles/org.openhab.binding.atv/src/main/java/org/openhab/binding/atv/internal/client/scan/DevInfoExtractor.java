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
package org.openhab.binding.atv.internal.client.scan;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Extracts device information fields from zeroconf service properties for a protocol.
 *
 * @author Dan Cunningham - Initial contribution
 */
@FunctionalInterface
@NonNullByDefault
public interface DevInfoExtractor {

    /**
     * Extracts device information from service properties.
     *
     * @param serviceType zeroconf service type the properties were announced under
     * @param properties zeroconf service properties
     * @return device information fields (keys as used by the protocol modules:
     *         {@code os}, {@code version}, {@code build_number}, {@code model},
     *         {@code raw_model}, {@code mac}, {@code output_device_id})
     */
    Map<String, Object> extract(String serviceType, Map<String, String> properties);
}
