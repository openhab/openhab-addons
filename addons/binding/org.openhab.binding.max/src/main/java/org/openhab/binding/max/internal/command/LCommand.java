/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.command;

/**
 * The {@link LCommand} request a status update for MAX! devices.
 *
 * @author Marcel Verpaalen - Initial Contribution
 */
public class LCommand extends CubeCommand {

    @Override
    public String getCommandString() {
        return "l:" + '\r' + '\n';
    }

    @Override
    public String getReturnStrings() {
        return "L:";
    }

}
