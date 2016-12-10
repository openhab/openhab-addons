/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vallox.internal.exceptions;

/**
 * Indicates an exception where a telegram is received but is
 * malformed in some way and may not be processed further.
 *
 * @author Hauke Fuhrmann - Initial contribution
 *
 */
public class MalformedTelegramException extends Exception {

    public MalformedTelegramException(String message) {
        super(message);
    }

    private static final long serialVersionUID = -4292987672906522240L;

}
