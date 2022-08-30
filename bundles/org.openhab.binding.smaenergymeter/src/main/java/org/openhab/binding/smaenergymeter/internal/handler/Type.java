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
package org.openhab.binding.smaenergymeter.internal.handler;

/**
 * The {@link Type} enum holds information of what type of measurement a channel is
 *
 * @author Lars Repenning - Initial contribution
 */
public enum Type {
    CURRENT(4),
    TOTAL(8),
    VERSION(4),
    UNKNOWN(0);

    private int dataSize;

    Type(int dataSize) {
        this.dataSize = dataSize;
    }

    public int getDataSize() {
        return dataSize;
    }
}
