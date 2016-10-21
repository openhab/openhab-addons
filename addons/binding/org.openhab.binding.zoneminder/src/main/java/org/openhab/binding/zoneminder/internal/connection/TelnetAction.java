/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.connection;

public enum TelnetAction {
    ON("on"),
    OFF("off");

    private final String fieldDescription;

    private TelnetAction(String value) {
        fieldDescription = value;
    }

    @Override
    public String toString() {
        return fieldDescription;
    }

    public static TelnetAction getEnum(String value) {
        return TelnetAction.valueOf(value.toUpperCase());
    }
}
