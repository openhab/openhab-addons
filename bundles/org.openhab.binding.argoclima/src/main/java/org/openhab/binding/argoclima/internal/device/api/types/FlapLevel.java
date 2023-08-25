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
 * Type representing Argo devices flap setting (swing). Int values are matching device's API.
 * <p>
 * Note: While this is supported by Argo remote protocol, the Ulisse device doesn't react to these settings
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public enum FlapLevel implements IArgoApiEnum {
    AUTO(0),
    LEVEL_1(1),
    LEVEL_2(2),
    LEVEL_3(3),
    LEVEL_4(4),
    LEVEL_5(5),
    LEVEL_6(6),
    LEVEL_7(7);

    private int value;

    FlapLevel(int intValue) {
        this.value = intValue;
    }

    @Override
    public int getIntValue() {
        return this.value;
    }
}
