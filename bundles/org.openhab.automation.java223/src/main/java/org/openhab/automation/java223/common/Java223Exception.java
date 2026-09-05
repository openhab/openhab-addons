/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.automation.java223.common;

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Unchecked exception wrapper
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class Java223Exception extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 7608058563887813804L;

    public Java223Exception(String message, Exception source) {
        super(message, source);
    }

    public Java223Exception(String message) {
        super(message);
    }
}
