/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.radiora.protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Raise (RAISE)
 * Begin Raising a Phantom Button.
 *
 * @author Jeff Lauterbach
 *
 */
public class RaiseCommand extends RadioRACommand {

    private int buttonNumber;

    public RaiseCommand(int buttonNumber) {
        this.buttonNumber = buttonNumber;
    }

    @Override
    public String getCommand() {
        return "RAISE";
    }

    @Override
    public List<String> getArgs() {
        List<String> args = new ArrayList<>();

        args.add(String.valueOf(buttonNumber));

        return args;

    }

}
