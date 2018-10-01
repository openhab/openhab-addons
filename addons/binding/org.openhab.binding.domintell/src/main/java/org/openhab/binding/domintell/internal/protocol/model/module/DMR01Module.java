/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol.model.module;

import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.model.SerialNumber;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;

/**
* The {@link DMR01Module} class is model class for DMR01 Domintell module
*
* @author Gabor Bicskei - Initial contribution
*/
public class DMR01Module extends ReleyModule {
    public DMR01Module(DomintellConnection connection, SerialNumber serialNumber) {
        super(connection, ModuleType.DMR, serialNumber, 5);
    }
}
