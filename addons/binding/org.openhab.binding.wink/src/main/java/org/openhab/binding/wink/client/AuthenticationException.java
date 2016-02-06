/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.client;

/**
 * Exception that is thrown when unable to authenticate to wink api
 *
 * @author Shawn Crosby
 *
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String string) {
        super(string);
    }

    /**
     * serialization version ID
     */
    private static final long serialVersionUID = 1L;

}
