/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hdpowerview.internal.api.requests;

import org.openhab.binding.hdpowerview.internal.api.ShadePosition;

/**
 * The position of a shade to set
 *
 * @author Andy Lintner
 */
class ShadeIdPosition {

    int id;
    ShadePosition positions;

    public ShadeIdPosition(int id, ShadePosition position) {
        this.id = id;
        this.positions = position;
    }
}
