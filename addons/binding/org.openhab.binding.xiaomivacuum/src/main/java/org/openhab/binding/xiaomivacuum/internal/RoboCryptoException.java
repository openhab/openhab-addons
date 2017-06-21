/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.xiaomivacuum.internal;

/**
 * Will be thrown instead of the many possible errors in the crypto module
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class RoboCryptoException extends Exception {

    public RoboCryptoException() {
        super();
    }

    public RoboCryptoException(String arg0) {
        super(arg0);
    }

    /**
     * required variable to avoid IncorrectMultilineIndexException warning
     */
    private static final long serialVersionUID = -1280858607995252320L;
}
