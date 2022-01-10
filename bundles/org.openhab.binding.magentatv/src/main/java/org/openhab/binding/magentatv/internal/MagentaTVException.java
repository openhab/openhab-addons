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
package org.openhab.binding.magentatv.internal;

import java.text.MessageFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MagentaTVException} class a binding specific exception class.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class MagentaTVException extends Exception {

    private static final long serialVersionUID = 6214176461907613559L;

    public MagentaTVException(String message) {
        super(message);
    }

    public MagentaTVException(Exception cause) {
        super(cause);
    }

    public MagentaTVException(Exception e, String message, Object... a) {
        super(MessageFormat.format(message, a) + " (" + e.getClass() + ": " + e.getMessage() + ")", e);
    }
}
