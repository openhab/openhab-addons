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
 * Indicates exceptions where too little data is received by
 * a remote peer to form a valid telegram.
 *
 * @author Hauke Fuhrmann - Initial contribution
 *
 */
public class InsufficientDataException extends Exception {

    public InsufficientDataException(String message) {
        super(message);
    }

    private static final long serialVersionUID = -4032987672908522240L;

}
