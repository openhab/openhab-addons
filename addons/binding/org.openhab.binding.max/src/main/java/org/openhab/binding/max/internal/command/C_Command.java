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
 * The {@link C_CubeCommand} to request configuration of a new MAX! device after inclusion.
 *
 * @author Marcel Verpaalen - Initial Contribution
 * @since 2.0
 *
 */

public class C_Command extends CubeCommand {

    private String rfAddress;

    public C_Command(String rfAddress) {
        this.rfAddress = rfAddress;
    }

    @Override
    public String getCommandString() {
        String cmd = "c:" + rfAddress + '\r' + '\n';
        return cmd;
    }

    @Override
    public String getReturnStrings() {
        return "C:";
    }

}
