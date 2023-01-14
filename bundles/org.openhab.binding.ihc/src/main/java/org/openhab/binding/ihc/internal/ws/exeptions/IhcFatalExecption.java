/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.ihc.internal.ws.exeptions;

/**
 * Exception for handling fatal communication errors to controller.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcFatalExecption extends IhcExecption {

    private static final long serialVersionUID = -8107948791816894352L;

    public IhcFatalExecption() {
    }

    public IhcFatalExecption(String message) {
        super(message);
    }

    public IhcFatalExecption(String message, Throwable cause) {
        super(message, cause);
    }

    public IhcFatalExecption(Throwable cause) {
        super(cause);
    }
}
