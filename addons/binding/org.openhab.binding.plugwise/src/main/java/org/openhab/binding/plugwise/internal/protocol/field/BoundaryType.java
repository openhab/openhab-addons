/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol.field;

/**
 * The boundary type that a Sense uses for switching.
 *
 * @author Wouter Born - Initial contribution
 */
public enum BoundaryType {

    HUMIDITY(0),
    TEMPERATURE(1),
    NONE(2);

    private final int identifier;

    BoundaryType(int identifier) {
        this.identifier = identifier;
    }

    public int toInt() {
        return identifier;
    }

}
