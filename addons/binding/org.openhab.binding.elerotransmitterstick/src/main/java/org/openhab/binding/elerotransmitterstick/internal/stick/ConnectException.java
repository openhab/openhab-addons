/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.elerotransmitterstick.internal.stick;

/**
 * The {@link ConnectException} is thrown on errors connecting to the elero transmitter stick.
 *
 * @author Volker Bier - Initial contribution
 */
public class ConnectException extends Exception {
    private static final long serialVersionUID = 946529257121090885L;

    public ConnectException(Throwable cause) {
        super(cause);
    }

    public ConnectException(String message) {
        super(message);
    }

    public ConnectException(String message, Exception e) {
        super(message, e);
    }
}
