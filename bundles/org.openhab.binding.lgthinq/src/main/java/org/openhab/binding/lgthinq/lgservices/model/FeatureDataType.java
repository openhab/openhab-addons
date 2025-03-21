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
package org.openhab.binding.lgthinq.lgservices.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link FeatureDataType}
 * Feature is the values the device has to expose its sensor attributes
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public enum FeatureDataType {
    ENUM,
    RANGE,
    BOOLEAN,
    BIT,
    REFERENCE,
    UNDEF;

    public static FeatureDataType fromValue(String value) {
        switch (value.toLowerCase()) {
            case "enum":
                return ENUM;
            case "boolean":
                return BOOLEAN;
            case "bit":
                return BIT;
            case "range":
                return RANGE;
            case "reference":
                return REFERENCE;
            default:
                return UNDEF;
        }
    }
}
