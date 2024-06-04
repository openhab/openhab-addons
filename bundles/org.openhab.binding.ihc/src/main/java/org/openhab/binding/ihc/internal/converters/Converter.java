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
package org.openhab.binding.ihc.internal.converters;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.ihc.internal.ws.exeptions.ConversionException;

/**
 * IHC / ELKO {@literal <->} openHAB data type converter interface.
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface Converter<R, T> {
    T convertFromResourceValue(@NonNull R from, @NonNull ConverterAdditionalInfo convertData)
            throws ConversionException;

    R convertFromOHType(@NonNull T from, @NonNull R value, @NonNull ConverterAdditionalInfo convertData)
            throws ConversionException;
}
