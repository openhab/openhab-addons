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
package org.openhab.binding.hdpowerview.internal.api.requests;

import org.openhab.binding.hdpowerview.internal.api.ShadePosition;

/**
 * The position of a shade to set
 *
 * @author Andy Lintner - Initial contribution
 */
class ShadeIdPosition {

    int id;
    ShadePosition positions;

    public ShadeIdPosition(int id, ShadePosition position) {
        this.id = id;
        this.positions = position;
    }
}
