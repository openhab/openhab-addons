/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.groupepsa.internal.rest.api.dto;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class BatteryStatus {

    private @Nullable BigDecimal capacity;
    private @Nullable Health health;

    public @Nullable BigDecimal getCapacity() {
        return capacity;
    }

    public @Nullable Health getHealth() {
        return health;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("capacity", capacity).append("health", health).toString();
    }
}
