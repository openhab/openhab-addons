/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hdpowerview.internal.api.requests;

import org.openhab.binding.hdpowerview.internal.api.ShadePosition;

/**
 * A request to set the position of a shade
 *
 * @author Andy Lintner - Initial contribution
 */
public class ShadeMove {

    ShadeIdPosition shade;

    public ShadeMove(String id, ShadePosition position) {
        this.shade = new ShadeIdPosition(id, position);
    }
}
