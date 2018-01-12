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
 * The motion sensitivity range of a Scan.
 *
 * @author Wouter Born - Initial contribution
 */
public enum Sensitivity {

    HIGH(0x14),
    MEDIUM(0x1E),
    OFF(0xFF);

    private final int value;

    Sensitivity(int value) {
        this.value = value;
    }

    public int toInt() {
        return value;
    }

}
