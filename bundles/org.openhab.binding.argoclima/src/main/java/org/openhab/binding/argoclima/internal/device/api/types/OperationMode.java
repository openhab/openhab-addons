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
package org.openhab.binding.argoclima.internal.device.api.types;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Type representing Argo HVAC operation mode (int values are matching device's API)
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public enum OperationMode implements IArgoApiEnum {
    COOL(1),
    DRY(2),
    WARM(3),
    FAN(4),
    AUTO(5);

    private int value;

    OperationMode(int intValue) {
        this.value = intValue;
    }

    @Override
    public int getIntValue() {
        return this.value;
    }
}
