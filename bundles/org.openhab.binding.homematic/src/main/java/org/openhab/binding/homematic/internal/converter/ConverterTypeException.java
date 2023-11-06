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
package org.openhab.binding.homematic.internal.converter;

/**
 * Exception if converting between two types is not possible due wrong item type or command.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class ConverterTypeException extends ConverterException {
    private static final long serialVersionUID = 7114173349077221055L;

    public ConverterTypeException(String message) {
        super(message);
    }
}
