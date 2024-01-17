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
package org.openhab.binding.webthing.internal.link;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link UnknownPropertyException} indicates addressing a WebThing property that does not exist
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
public class UnknownPropertyException extends Exception {
    private static final long serialVersionUID = -5302763943749264616L;

    /**
     * contructor
     * 
     * @param message the error message
     */
    UnknownPropertyException(String message) {
        super(message);
    }
}
