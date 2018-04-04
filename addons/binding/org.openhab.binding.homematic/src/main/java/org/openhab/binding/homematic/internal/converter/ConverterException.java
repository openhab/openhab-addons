/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.converter;

/**
 * Exception if something goes wrong when converting values between openHAB and the binding.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class ConverterException extends Exception {
    private static final long serialVersionUID = 78045670450002L;

    public ConverterException(String message) {
        super(message);
    }

}
