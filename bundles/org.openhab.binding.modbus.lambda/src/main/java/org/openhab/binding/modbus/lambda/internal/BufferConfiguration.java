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
package org.openhab.binding.modbus.lambda.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link BufferConfiguration} class contains fields mapping
 * thing configuration parameters.
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 */
@NonNullByDefault
public class BufferConfiguration extends GeneralConfiguration {
    /**
     * Subindex to calculate the base adress of the modbus registers
     */
    private int subindex = 0;

    public int getSubindex() {
        return subindex;
    }

    public void setSubindex(int subindex) {
        this.subindex = subindex;
    }
}
