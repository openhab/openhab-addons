/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.opendaikin.internal;

import java.io.IOException;

/**
 * Exception for when an unexpected response is received from the Daikin controller.
 *
 * @author Tim Waterhouse <tim@timwaterhouse.com> - Initial contribution
 *
 */
public class OpenDaikinCommunicationException extends IOException {
    private static final long serialVersionUID = 529232811860854017L;

    public OpenDaikinCommunicationException(String message) {
        super(message);
    }

    public OpenDaikinCommunicationException(Throwable ex) {
        super(ex);
    }

    public OpenDaikinCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
