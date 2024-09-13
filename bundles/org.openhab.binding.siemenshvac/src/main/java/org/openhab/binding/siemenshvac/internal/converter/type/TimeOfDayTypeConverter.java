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
package org.openhab.binding.siemenshvac.internal.converter.type;

import java.time.ZonedDateTime;
import java.util.Locale;

import javax.measure.Unit;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.converter.ConverterException;
import org.openhab.binding.siemenshvac.internal.metadata.SiemensHvacMetadataDataPoint;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;

import com.google.gson.JsonElement;

/**
 * Converts between a SiemensHvac datapoint value and an openHAB DecimalType.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class TimeOfDayTypeConverter extends AbstractTypeConverter {
    private final TimeZoneProvider timeZoneProvider;

    public TimeOfDayTypeConverter(final TimeZoneProvider timeZoneProvider) {
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    protected boolean toBindingValidation(Type type) {
        return true;
    }

    @Override
    protected @Nullable Object toBinding(Type type, ChannelType tp) throws ConverterException {
        Object valUpdate = null;

        if (type instanceof DateTimeType dateTime) {
            valUpdate = dateTime.toString();
        }

        return valUpdate;
    }

    @Override
    protected boolean fromBindingValidation(JsonElement value, String unit, String type) {
        return true;
    }

    @Override
    protected State fromBinding(JsonElement value, String unit, String type, ChannelType tp, Locale locale)
            throws ConverterException {
        if ("----".equals(value.getAsString())) {
            return new DateTimeType(ZonedDateTime.now(this.timeZoneProvider.getTimeZone()));
        } else {
            if ("h:m".equals(unit)) {
                String st = value.getAsString();
                String[] parts = st.split(":");
                int h = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);

                Unit<Time> targetUnit = Units.MINUTE;
                return new QuantityType<>(h * 60 + m, targetUnit);

            } else if ("m:s".equals(unit)) {
                String st = value.getAsString();
                String[] parts = st.split(":");
                int m = Integer.parseInt(parts[0]);
                int s = Integer.parseInt(parts[1]);

                Unit<Time> targetUnit = Units.SECOND;
                return new QuantityType<>(m * 60 + s, targetUnit);

            } else if ("h".equals(unit)) {
                int val = Integer.parseInt(value.getAsString());

                Unit<Time> targetUnit = Units.HOUR;
                return new QuantityType<>(val, targetUnit);

            } else {
                throw new ConverterException("unsupported unit type:" + unit);
            }
        }
    }

    @Override
    public String getChannelType(SiemensHvacMetadataDataPoint dpt) {
        return "number";
    }

    @Override
    public String getItemType(SiemensHvacMetadataDataPoint dpt) {
        return CoreItemFactory.NUMBER + ":Time";
    }

    @Override
    public boolean hasVariant() {
        return false;
    }
}
