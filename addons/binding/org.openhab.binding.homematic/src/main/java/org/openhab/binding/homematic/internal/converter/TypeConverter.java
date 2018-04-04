/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
