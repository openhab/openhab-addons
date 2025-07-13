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
package org.openhab.binding.fenecon.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AddressComponent} is a container class to identify a component of a {@link Address}.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public record AddressComponent(String component) {
    public AddressComponent(String component) {
        this.component = convertComponentWithRegEx(component);
    }

    // Bundle same components with regex if possible, to reduce the number of requests
    private static String convertComponentWithRegEx(String component) {
        return component.replaceFirst("\\d$", ".+");
    }
}
