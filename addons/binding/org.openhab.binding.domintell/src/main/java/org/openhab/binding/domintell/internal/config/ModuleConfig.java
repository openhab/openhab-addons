/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
