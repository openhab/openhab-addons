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
package org.openhab.binding.xmppclient.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link XMPPClientConfigException} represents a binding specific {@link Exception}.
 *
 * @author Leo Siepel - Initial contribution
 */

@NonNullByDefault
public class XMPPClientConfigException extends Exception {

    private static final long serialVersionUID = 1L;

    public XMPPClientConfigException(String message) {
        super(message);
    }

    public XMPPClientConfigException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public XMPPClientConfigException(@Nullable Throwable cause) {
        super(cause);
    }
}
