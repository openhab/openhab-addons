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
 * The {@link A_Command} deletes the device and room configuration from the Cube.
 *
 * @author Marcel Verpaalen - Initial Contribution
 * @since 2.0
 */

public class A_Command extends CubeCommand {

    @Override
    public String getCommandString() {
        return "a:" + '\r' + '\n';
    }

    @Override
    public String getReturnStrings() {
        return "A:";
    }

}
