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
package org.openhab.binding.homeconnect.internal.client.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * AvailableProgramOption model
 *
 * @author Jonas Brüstel - Initial contribution
 *
 */
@NonNullByDefault
public class AvailableProgramOption {

    private final String key;
    private final List<String> allowedValues;

    public AvailableProgramOption(String key, List<String> allowedValues) {
        this.key = key;
        this.allowedValues = allowedValues;
    }

    public String getKey() {
        return key;
    }

    public List<String> getAllowedValues() {
        return allowedValues;
    }

    @Override
    public String toString() {
        return "AvailableProgramOption [key=" + key + ", allowedValues=" + allowedValues + "]";
    }
}
