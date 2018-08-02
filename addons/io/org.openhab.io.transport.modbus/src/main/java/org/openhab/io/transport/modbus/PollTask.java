/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Poll task represents Modbus read request
 *
 * Must be hashable. HashCode and equals should be defined such that no two poll tasks are registered that are
 * equal.
 *
 * @author Sami Salonen - Initial contribution
 *
 * @see ModbusManager.registerRegularPoll
 */
@NonNullByDefault
public interface PollTask extends TaskWithEndpoint<ModbusReadRequestBlueprint, ModbusReadCallback> {
    @Override
    default int getMaxTries() {
        return getRequest().getMaxTries();
    }
}
