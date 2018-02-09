/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.misc;

/**
 * Exception if something happens in the communication to the Homematic gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HomematicClientException extends Exception {
    private static final long serialVersionUID = 76348991234346L;

    public HomematicClientException(String message) {
        super(message);
    }

    public HomematicClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
