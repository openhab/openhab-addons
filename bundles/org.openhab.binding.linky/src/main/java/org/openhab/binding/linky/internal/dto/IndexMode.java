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
package org.openhab.binding.linky.internal.dto;

/**
 * The {@link IndexMode} represents the index type : Supplier or Distributor
 *
 * @author Laurent Arnal - Initial contribution
 */

public enum IndexMode {
    NONE(-1, 0),
    SUPPLIER(0, 10),
    DISTRIBUTOR(1, 4);

    private final int idx;
    private final int size;

    IndexMode(int idx, int size) {
        this.idx = idx;
        this.size = size;
    }

    public int getIdx() {
        return idx;
    }

    public int getSize() {
        return size;
    }
}
