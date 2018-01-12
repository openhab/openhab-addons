/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.exceptions;

/**
 * Exception for when RFXCOM device has a timeout while processing a message
 *
 * @author Martin van Wingerden - Initial contribution
 */
public class RFXComTimeoutException extends RFXComException {
    public RFXComTimeoutException(String message) {
        super(message);
    }
}
