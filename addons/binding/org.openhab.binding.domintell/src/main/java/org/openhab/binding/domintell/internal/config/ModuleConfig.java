/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.domintell.internal.config;

import org.openhab.binding.domintell.internal.protocol.model.SerialNumber;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;

import java.math.BigDecimal;

/**
 * The {@link ModuleConfig} class contains configuration for module identification
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class ModuleConfig {
    private String moduleType;
    private BigDecimal serialNumber;

    //getters
    public ModuleType getModuleType() {
        return ModuleType.valueOf(moduleType);
    }

    public SerialNumber getSerialNumber() {
        return serialNumber != null ? new SerialNumber(serialNumber.intValue()): null;
    }
}
