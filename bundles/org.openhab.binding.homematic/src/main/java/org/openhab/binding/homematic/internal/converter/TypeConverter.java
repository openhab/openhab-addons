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
package org.openhab.binding.homematic.internal.converter;

import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;

/**
 * Converter interface for converting between openHAB states/commands and Homematic values.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface TypeConverter<T extends State> {

    /**
     * Converts an openHAB type to a Homematic value.
     */
    Object convertToBinding(Type type, HmDatapoint dp) throws ConverterException;

    /**
     * Converts a Homematic value to an openHAB type.
     */
    T convertFromBinding(HmDatapoint dp) throws ConverterException;
}
