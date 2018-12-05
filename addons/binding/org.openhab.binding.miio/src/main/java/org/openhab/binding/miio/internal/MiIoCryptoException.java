/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miio.internal;

/**
 * Will be thrown instead of the many possible errors in the crypto module
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MiIoCryptoException extends Exception {

    public MiIoCryptoException() {
        super();
    }

    public MiIoCryptoException(String arg0) {
        super(arg0);
    }

    /**
     * required variable to avoid IncorrectMultilineIndexException warning
     */
    private static final long serialVersionUID = -1280858607995252320L;
}
