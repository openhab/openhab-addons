/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mercedesme.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MercedesMeBindingException} is thrown if anything occurs which prevents the binding to operate
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MercedesMeBindingException extends Exception {

    private static final long serialVersionUID = 6675357782577233939L;

    public MercedesMeBindingException(String message) {
        super(message);
    }
}
