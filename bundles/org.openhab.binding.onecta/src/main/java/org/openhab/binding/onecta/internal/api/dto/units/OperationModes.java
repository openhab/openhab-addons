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
package org.openhab.binding.onecta.internal.api.dto.units;

import org.openhab.binding.onecta.internal.api.Enums;

/**
 * @author Alexander Drent - Initial contribution
 */
public class OperationModes {
    private OpertationMode heating;
    private OpertationMode cooling;
    private OpertationMode auto;

    public OpertationMode getOperationMode(Enums.OperationMode operationMode) {
        if (operationMode.equals(Enums.OperationMode.HEAT)) {
            return this.heating;
        } else if (operationMode.equals(Enums.OperationMode.COLD)) {
            return this.cooling;
        } else if (operationMode.equals(Enums.OperationMode.AUTO)) {
            return this.auto;
        } else
            return null;
    }
}
