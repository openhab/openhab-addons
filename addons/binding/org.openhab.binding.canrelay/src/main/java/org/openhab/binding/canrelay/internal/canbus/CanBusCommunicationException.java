/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.canbus;

/**
 * Generic error in CANBUS communication
 *
 * @author Lubos Housa - Initial Contribution
 */
public class CanBusCommunicationException extends Exception {

    private static final long serialVersionUID = 858600801960714203L;

    public CanBusCommunicationException(String message, Throwable t) {
        super(message, t);
    }
}
