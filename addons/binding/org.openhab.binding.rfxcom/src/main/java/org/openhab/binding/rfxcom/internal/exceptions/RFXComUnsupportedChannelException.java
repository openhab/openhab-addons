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
 * Exception to indicate that a request was received for an unsupported channel
 *
 * @author Martin van Wingerden
 */
public class RFXComUnsupportedChannelException extends RFXComException {
    public RFXComUnsupportedChannelException(String message) {
        super(message);
    }
}
