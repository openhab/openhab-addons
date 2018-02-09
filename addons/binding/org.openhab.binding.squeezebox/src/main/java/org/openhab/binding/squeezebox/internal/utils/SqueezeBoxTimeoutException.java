/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.squeezebox.internal.utils;

/***
 *
 * Exception class to indicate a timeout during comminication with
 * the media server.
 *
 * @author Patrik Gfeller
 *
 */
public class SqueezeBoxTimeoutException extends Exception {
    private static final long serialVersionUID = 4542388088266882905L;

    public SqueezeBoxTimeoutException(String message) {
        super(message);
    }
}
