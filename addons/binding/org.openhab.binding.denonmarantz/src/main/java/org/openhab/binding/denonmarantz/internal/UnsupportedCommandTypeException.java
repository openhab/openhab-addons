/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.denonmarantz.internal;

/**
 * Exception thrown when an unsupported command type is sent to a channel.
 *
 * @author Jan-Willem Veldhuis - Initial contribution
 *
 */
public class UnsupportedCommandTypeException extends Exception {

    private static final long serialVersionUID = 42L;

    public UnsupportedCommandTypeException() {
        super();
    }

    public UnsupportedCommandTypeException(String message) {
        super(message);
    }

}
