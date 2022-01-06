/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Will be thrown for cloud errors
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LinkyException extends Exception {

    private static final long serialVersionUID = 3703839284673384018L;

    public LinkyException() {
        super();
    }

    public LinkyException(String message) {
        super(message);
    }

    public LinkyException(String message, Exception e) {
        super(message, e);
    }
}
