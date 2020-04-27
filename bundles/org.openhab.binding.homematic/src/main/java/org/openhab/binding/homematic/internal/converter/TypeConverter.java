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
package org.openhab.binding.homematic.internal.converter;

import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.homematic.internal.model.HmDatapoint;

/**
 * Converter interface for converting between openHAB states/commands and Homematic values.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface TypeConverter<T extends State> {

    /**
     * Converts a openHAB type to a Homematic value.
     */
    public Object convertToBinding(Type type, HmDatapoint dp) throws ConverterException;

    /**
     * Converts a Homematic value to a openHAB type.
     */
    public T convertFromBinding(HmDatapoint dp) throws ConverterException;
}
