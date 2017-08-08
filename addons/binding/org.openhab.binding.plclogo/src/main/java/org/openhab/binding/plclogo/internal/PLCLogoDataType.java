/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plclogo.internal;

/**
 * The {@link PLCLogoDataType} describes data types supported.
 *
 * @author Alexander Falkenstern - Initial contribution
 */

public enum PLCLogoDataType {
    INVALID(-1),
    BIT(1),
    WORD(2),
    DWORD(4);

    private int count = -1;

    private PLCLogoDataType(final int count) {
        this.count = count;
    }

    public int getByteCount() {
        return count;
    }

}
