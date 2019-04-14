/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal.exceptions;

/**
 * The {@link ParadoxBindingException} Wrapper of Exception class. May not be needed.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class ParadoxBindingException extends Exception {

    private static final long serialVersionUID = -5771699322577106346L;

    public ParadoxBindingException() {
        super();
    }

    public ParadoxBindingException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ParadoxBindingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParadoxBindingException(String message) {
        super(message);
    }

    public ParadoxBindingException(Throwable cause) {
        super(cause);
    }

}
