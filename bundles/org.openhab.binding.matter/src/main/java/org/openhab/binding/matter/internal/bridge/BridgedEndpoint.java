/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.bridge;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents a bridged endpoint.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class BridgedEndpoint {
    public final String deviceType;
    public final String id;
    public final String nodeLabel;
    public final String productName;
    public final String productLabel;
    public final String serialNumber;
    public final Map<String, Map<String, Object>> attributeMap;

    public BridgedEndpoint(String deviceType, String id, String nodeLabel, String productName, String productLabel,
            String serialNumber, Map<String, Map<String, Object>> attributeMap) {
        this.deviceType = deviceType;
        this.id = id;
        this.nodeLabel = nodeLabel;
        this.productName = productName;
        this.productLabel = productLabel;
        this.serialNumber = serialNumber;
        this.attributeMap = attributeMap;
    }
}
