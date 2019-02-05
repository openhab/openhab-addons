/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.mysensors.internal.exception;

/**
 * Exception occurs if there is no content to add, for example in a variable.
 *
 * @author Tim Oberföll
 *
 */
public class NoContentException extends Exception {
    private static final long serialVersionUID = -3446354234423332363L;

    public NoContentException(String message) {
        super(message);
    }
}
