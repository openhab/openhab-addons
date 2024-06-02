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
package org.openhab.binding.siemenshvac.internal.converter;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.metadata.SiemensHvacMetadataDataPoint;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;

import com.google.gson.JsonObject;

/**
 * Converter interface for converting between openHAB states/commands and siemensHvac values.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public interface TypeConverter {

    /**
     * Converts an openHAB type to a SiemensHVac value.
     */
    @Nullable
    Object convertToBinding(Type type, ChannelType tp) throws ConverterException;

    /**
     * Converts a siemensHvac value to an openHAB type.
     */
    State convertFromBinding(JsonObject dp, ChannelType tp, Locale locale) throws ConverterException;

    /**
     * get underlying channel type to construct channel type UID
     *
     */
    String getChannelType(SiemensHvacMetadataDataPoint dpt);

    /**
     * get underlying item type on openhab side for this SiemensHvac type
     *
     */
    String getItemType(SiemensHvacMetadataDataPoint dpt);

    /**
     * tell if this type have different subvariant or not
     *
     */
    boolean hasVariant();
}
