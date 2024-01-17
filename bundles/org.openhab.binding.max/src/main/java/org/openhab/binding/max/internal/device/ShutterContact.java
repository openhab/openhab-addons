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
package org.openhab.binding.max.internal.device;

import org.openhab.core.library.types.OpenClosedType;

/**
 * MAX! Shutter contact device.
 *
 * @author Andreas Heil - Initial contribution
 * @author Marcel Verpaalen - OH2 update
 */
public class ShutterContact extends Device {

    private OpenClosedType shutterState;

    public ShutterContact(DeviceConfiguration c) {
        super(c);
    }

    public void setShutterState(OpenClosedType shutterState) {
        if (this.shutterState != shutterState) {
            setUpdated(true);
        }
        this.shutterState = shutterState;
    }

    public OpenClosedType getShutterState() {
        return shutterState;
    }

    @Override
    public DeviceType getType() {
        return DeviceType.ShutterContact;
    }
}
