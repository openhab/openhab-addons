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
package org.openhab.binding.onewire.internal.owserver;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OwserverControlFlag} provides the owserver protocol control flag
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public enum OwserverControlFlag {
    UNCACHED(0x00000020),
    SAFEMODE(0x00000010),
    ALIAS(0x00000008),
    PERSISTENCE(0x00000004),
    BUS_RET(0x00000002),
    DEVICE_DISPLAY(0x00000000),
    OWNET(0x00000100);

    private final int controlFlag;

    OwserverControlFlag(int controlFlag) {
        this.controlFlag = controlFlag;
    }

    /**
     * get the this flag's numeric representation
     *
     * @return integer value of this flag
     */
    public int getValue() {
        return controlFlag;
    }

    /**
     * check if a this flag is set in the parameter
     *
     * @param controlFlags full control flag
     * @return true if this flag is set in the parameter
     */
    public boolean isSet(int controlFlags) {
        return (this.getValue() & controlFlags) == this.getValue();
    }
}
