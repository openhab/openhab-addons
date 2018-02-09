/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.device;

import org.eclipse.smarthome.core.library.types.OnOffType;

/**
 * MAX! EcoSwitch.
 *
 * @author Marcel Verpaalen
 * @since 1.6.0
 */

public class EcoSwitch extends ShutterContact {

    private OnOffType ecoMode = null;

    /**
     * Class constructor.
     *
     * @param c
     */
    public EcoSwitch(DeviceConfiguration c) {
        super(c);
    }

    @Override
    public DeviceType getType() {
        return DeviceType.EcoSwitch;
    }

    public OnOffType getEcoMode() {
        return ecoMode;
    }

    public void setEcoMode(OnOffType ecoMode) {
        this.ecoMode = ecoMode;
    }
}
