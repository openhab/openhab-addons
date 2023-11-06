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
package org.openhab.binding.rotel.internal.protocol;

import java.util.EventObject;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Rotel event used to notify changes coming from messages received from the Rotel device
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RotelMessageEvent extends EventObject {

    private static final long serialVersionUID = 1L;
    private String key;
    private String value;

    public RotelMessageEvent(Object source, String key, String value) {
        super(source);
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
