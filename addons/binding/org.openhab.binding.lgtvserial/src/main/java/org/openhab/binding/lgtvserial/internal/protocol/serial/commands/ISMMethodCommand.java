/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

/**
 * This class handles the ISM method command.
 *
 * @author Richard Lavoie - Initial contribution
 *
 */
public class ISMMethodCommand extends BaseStringCommand {

    protected ISMMethodCommand(int setId) {
        super('j', 'p', setId);
    }

}
