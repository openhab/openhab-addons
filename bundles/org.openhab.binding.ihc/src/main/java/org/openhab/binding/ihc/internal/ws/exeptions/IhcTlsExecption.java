/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * Exception for handling TLS communication errors to controller.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcTlsExecption extends IhcFatalExecption {

    private static final long serialVersionUID = -1366186910684967044L;

    public IhcTlsExecption() {
    }

    public IhcTlsExecption(String message) {
        super(message);
    }

    public IhcTlsExecption(String message, Throwable cause) {
        super(message, cause);
    }

    public IhcTlsExecption(Throwable cause) {
        super(cause);
    }
}
