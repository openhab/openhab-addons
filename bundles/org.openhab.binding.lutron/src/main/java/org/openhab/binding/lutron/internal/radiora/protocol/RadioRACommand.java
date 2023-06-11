/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.radiora.protocol;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Abstract base class for commands.
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
@NonNullByDefault
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
