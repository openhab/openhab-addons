/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal.protocol;

/**
 * The {@link CommandUnsuccessfulException} should be raised when the Hyperion server
 * rejects a command.
 *
 * @author Daniel Walters - Initial contribution
 */
public class CommandUnsuccessfulException extends Exception {

    private static final long serialVersionUID = 1421923610566223857L;

    public CommandUnsuccessfulException(String message) {
        super(message);
    }
}
