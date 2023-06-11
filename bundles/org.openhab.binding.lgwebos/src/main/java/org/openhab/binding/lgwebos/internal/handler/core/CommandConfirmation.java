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
package org.openhab.binding.lgwebos.internal.handler.core;

/**
 * {@link CommandConfirmation} represents payload in response from TV were it only confirms the result of an operation.
 *
 * @author Sebastian Prehn - Initial contribution
 */
public class CommandConfirmation {
    private boolean returnValue;

    public CommandConfirmation() {
        // no-argument constructor for gson
    }

    public CommandConfirmation(boolean returnValue) {
        this.returnValue = returnValue;
    }

    public boolean getReturnValue() {
        return returnValue;
    }

    @Override
    public String toString() {
        return "CommandConfirmation [returnValue=" + returnValue + "]";
    }
}
