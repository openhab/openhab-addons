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
package org.openhab.binding.somfytahoma.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SomfyTahomaState} holds information about
 * device state.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaState {

    private Object value = new Object();
    private int type;
    private String name = "";

    public Object getValue() {
        return value;
    }
    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", value=" + value +
                '}';
    }
}
