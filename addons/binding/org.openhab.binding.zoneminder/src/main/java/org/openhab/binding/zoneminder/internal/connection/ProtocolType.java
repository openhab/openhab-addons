/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.connection;

public enum ProtocolType {
    HTTP("http"),
    HTTPS("https");

    private final String description;

    private ProtocolType(String value) {
        description = value;
    }

    @Override
    public String toString() {
        return description;
    }

    public static ProtocolType getEnum(String value) {
        return ProtocolType.valueOf(value.toUpperCase());
    }
}
