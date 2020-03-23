/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.fox.internal.core;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link FoxException} is a generic Fox access exception.
 *
 * @author Kamil Subzda - Initial contribution
 */
@NonNullByDefault
public class FoxException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public FoxException(String message) {
        super(message);
    }
}
