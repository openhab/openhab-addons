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
package org.openhab.binding.vigicrues.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception for errors when using the VigiCrues API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VigiCruesException extends Exception {
    private static final long serialVersionUID = -7781683052187130152L;

    public VigiCruesException(Throwable e) {
        super(null, e);
    }

    public VigiCruesException(String msg) {
        super(msg, null);
    }
}
