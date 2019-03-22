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
package org.openhab.binding.domintell.internal.protocol.model.module;

import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.model.SerialNumber;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;

/**
 * The {@link DPBL06Module} class is handling physical push button module DPBL06
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class DPBL06Module extends PushButtonModule {
    /**
     * Constructor
     *
     * @param connection Connection
     * @param serialNumber Module serial number
     */
    public DPBL06Module(DomintellConnection connection, SerialNumber serialNumber) {
        super(connection, ModuleType.B86, serialNumber, 6);
    }
}
