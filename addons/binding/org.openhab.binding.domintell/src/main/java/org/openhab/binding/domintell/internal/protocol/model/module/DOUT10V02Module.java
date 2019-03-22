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
* The {@link DOUT10V02Module} class is model class of DOUT10V02 module
*
* @author Gabor Bicskei - Initial contribution
*/
public class DOUT10V02Module extends DimmerModule {
    public DOUT10V02Module(DomintellConnection connection, SerialNumber serialNumber) {
        super(connection, ModuleType.D10, serialNumber, 1);
    }
}
