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
package org.openhab.binding.kaleidescape.internal.communication;

import java.util.EventObject;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * KaleidescapeMessageEvent used to notify changes coming from messages received from the Kaleidescape component
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class KaleidescapeMessageEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    private final String key;
    private final String value;
    private final boolean isCached;

    public KaleidescapeMessageEvent(Object source, String key, String value, boolean isCached) {
        super(source);
        this.key = key;
        this.value = value;
        this.isCached = isCached;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public boolean isCached() {
        return isCached;
    }
}
