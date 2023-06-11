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
package org.openhab.binding.somfytahoma.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SomfyTahomaDeviceDefinitionState} holds information about states
 * provided by a device.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaDeviceDefinitionState {

    private String qualifiedName = "";
    private List<String> values = new ArrayList<>();

    public String getQualifiedName() {
        return qualifiedName;
    }

    public List<String> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return qualifiedName + " (values: " + String.join(", ", values) + ")";
    }
}
