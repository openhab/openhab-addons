/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.radiora.protocol;

import java.util.List;

/**
 * Abstract base class for commands.
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
public abstract class RadioRACommand {

    protected static final String FIELD_SEPARATOR = ",";
    protected static final String CMD_TERMINATOR = "\r";

    public abstract String getCommand();

    public abstract List<String> getArgs();

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append(getCommand());

        for (String arg : getArgs()) {
            str.append(FIELD_SEPARATOR);
            str.append(arg);
        }

        str.append(CMD_TERMINATOR);

        return str.toString();
    }

}
