/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HttpException} is used for http error codes
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class HttpException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    int code;

    public int getCode() {
        return code;
    }

    public HttpException(int code, String message) {
        super(message);
        this.code = code;
    }
}
