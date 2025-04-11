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
package org.openhab.binding.matter.internal.controller.devices.types;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;

/**
 * The {@link GenericType} is a generic type of device and can be used for most Matter devices
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class GenericType extends DeviceType {

    public GenericType(Integer deviceType, MatterBaseThingHandler handler, Integer endpointNumber) {
        super(deviceType, handler, endpointNumber);
    }
}
