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
 * Indicates an exception where a telegram is received but was addressed to
 * someone else, so the telegram is not to be processed.
 *
 * @author Hauke Fuhrmann - Initial contribution
 *
 */
public class InvalidRecepientException extends Exception {

    public InvalidRecepientException(String message) {
        super(message);
    }

    private static final long serialVersionUID = -4032987672906522240L;

}
