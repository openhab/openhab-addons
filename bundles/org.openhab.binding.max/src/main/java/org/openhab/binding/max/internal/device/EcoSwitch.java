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
package org.openhab.binding.max.internal.device;

import org.openhab.core.library.types.OnOffType;

/**
 * MAX! EcoSwitch.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class EcoSwitch extends ShutterContact {

    private OnOffType ecoMode;

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
