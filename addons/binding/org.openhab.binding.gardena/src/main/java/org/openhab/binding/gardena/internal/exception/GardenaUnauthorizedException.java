/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.gardena.internal.exception;

/**
 * Exception for invalid user and password.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GardenaUnauthorizedException extends GardenaException {

    private static final long serialVersionUID = 4343137351443555679L;

    public GardenaUnauthorizedException(Throwable ex) {
        super(ex);
    }
}
