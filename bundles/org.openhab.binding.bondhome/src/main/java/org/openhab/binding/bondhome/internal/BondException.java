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
package org.openhab.binding.bondhome.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown for various API issues.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class BondException extends Exception {
    private static final long serialVersionUID = 1L;

    private boolean wasBridgeSetOffline;

    public BondException(String message) {
        this(message, false);
    }

    public BondException(String message, boolean wasBridgeSetOffline) {
        super(message);
        this.wasBridgeSetOffline = wasBridgeSetOffline;
    }

    public boolean wasBridgeSetOffline() {
        return wasBridgeSetOffline;
    }
}
