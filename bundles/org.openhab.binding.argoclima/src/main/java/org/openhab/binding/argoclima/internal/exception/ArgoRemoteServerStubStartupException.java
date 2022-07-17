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
package org.openhab.binding.argoclima.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The class {@code ArgoRemoteServerStubStartupException} is thrown in case of any issues when starting the stub Argo
 * server (for intercepting mode)
 *
 * @see {@link org.openhab.binding.argoclima.internal.device.passthrough.RemoteArgoApiServerStub
 *      RemoteArgoApiServerStub}
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class ArgoRemoteServerStubStartupException extends Exception {

    private static final long serialVersionUID = 3798832375487523670L;

    public ArgoRemoteServerStubStartupException(String message) {
        super(message);
    }

    public ArgoRemoteServerStubStartupException(String message, Throwable cause) {
        super(message, cause);
    }
}
