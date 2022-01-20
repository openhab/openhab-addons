/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.squeezebox.internal.utils;

/***
 *
 * Exception class to indicate a timeout during comminication with
 * the media server.
 *
 * @author Patrik Gfeller - Initial contribution
 *
 */
public class SqueezeBoxTimeoutException extends Exception {
    private static final long serialVersionUID = 4542388088266882905L;

    public SqueezeBoxTimeoutException(String message) {
        super(message);
    }
}
