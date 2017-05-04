/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal.exceptions;

/**
 * The {@link NoInternetRadioPresetFoundException} class is an exception
 *
 * @author Thomas Traunbauer
 */
public class NoInternetRadioPresetFoundException extends NoPresetFoundException {
    private static final long serialVersionUID = 1L;

    public NoInternetRadioPresetFoundException() {
        super();
    }

    public NoInternetRadioPresetFoundException(String message) {
        super(message);
    }

    public NoInternetRadioPresetFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoInternetRadioPresetFoundException(Throwable cause) {
        super(cause);
    }
}