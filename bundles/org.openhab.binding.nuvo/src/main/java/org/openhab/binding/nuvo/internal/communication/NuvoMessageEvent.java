/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nuvo.internal.communication;

import java.util.EventObject;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * NuvoMessageEvent event used to notify changes coming from messages received from the Nuvo device
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class NuvoMessageEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    private final String type;
    private final String zone;
    private final String src;
    private final String value;

    public NuvoMessageEvent(Object source, String type, String zone, String src, String value) {
        super(source);
        this.type = type;
        this.zone = zone;
        this.src = src;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getZone() {
        return zone;
    }

    public String getSrc() {
        return src;
    }

    public String getValue() {
        return value;
    }
}
