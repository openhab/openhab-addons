/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.fineoffsetweatherstation.internal.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;

/**
 * Parses a single measurand from a TCP payload or an HTTP {@code get_livedata_info} response.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class Measurand implements Parser {
    private final String name;
    private final String channelPrefix;
    private final MeasureType measureType;

    private @Nullable Map<ParserCustomizationType, ParserCustomization> customizations;
    private @Nullable ChannelTypeUID channelTypeUID;

    private @Nullable HttpSource httpSource;

    Measurand(String channelPrefix, String name, MeasureType measureType) {
        this.channelPrefix = channelPrefix;
        this.name = name;
        this.measureType = measureType;
    }

    Measurand http(HttpGroup group) {
        this.httpSource = new HttpSource(group, null, null, false);
        return this;
    }

    Measurand http(HttpGroup group, String key) {
        this.httpSource = new HttpSource(group, null, key, false);
        return this;
    }

    Measurand http(HttpGroup group, int httpCode) {
        this.httpSource = new HttpSource(group, httpCode, null, false);
        return this;
    }

    Measurand httpAlt(HttpGroup group, int httpCode) {
        this.httpSource = new HttpSource(group, httpCode, null, true);
        return this;
    }

    Measurand channelType(ChannelTypeUID channelTypeUID) {
        this.channelTypeUID = channelTypeUID;
        return this;
    }

    Measurand customization(ParserCustomizationType type, MeasureType measureType) {
        Map<ParserCustomizationType, ParserCustomization> map = customizations;
        if (map == null) {
            map = new HashMap<>();
            customizations = map;
        }
        map.put(type, new ParserCustomization(type, measureType));
        return this;
    }

    @Nullable
    HttpSource getHttpSource() {
        return httpSource;
    }

    String getChannelPrefix() {
        return channelPrefix;
    }

    @Override
    public int extractMeasuredValues(byte[] data, int offset, @Nullable Integer channel,
            @Nullable ParserCustomizationType customizationType, List<MeasuredValue> result,
            DebugDetails debugDetails) {
        MeasureType measureType = getMeasureType(customizationType);
        State state = measureType.toState(data, offset);
        if (state != null) {
            debugDetails.addDebugDetails(offset, measureType.getByteSize(),
                    measureType.name() + ": " + state.toFullString());
            ChannelTypeUID channelType = channelTypeUID == null ? measureType.getChannelTypeId() : channelTypeUID;
            result.add(new MeasuredValue(measureType, channelPrefix, channel, channelType, state, name));
        } else {
            debugDetails.addDebugDetails(offset, measureType.getByteSize(), measureType.name() + ": null");
        }
        return measureType.getByteSize();
    }

    public MeasureType getMeasureType(@Nullable ParserCustomizationType customizationType) {
        if (customizationType == null) {
            return measureType;
        }
        return Objects.requireNonNull(Optional.ofNullable(customizations).map(m -> m.get(customizationType))
                .map(ParserCustomization::getMeasureType).orElse(measureType));
    }

    @Nullable
    MeasuredValue parseHttp(String val, @Nullable String unit, @Nullable Integer channel,
            @Nullable ParserCustomizationType customizationType) {
        MeasureType measureType = getMeasureType(customizationType);
        State state = measureType.parseState(val, unit);
        if (state == null) {
            return null;
        }
        ChannelTypeUID channelType = channelTypeUID == null ? measureType.getChannelTypeId() : channelTypeUID;
        return new MeasuredValue(measureType, channelPrefix, channel, channelType, state, name);
    }

    static Measurand measurand(String channelPrefix, String name, MeasureType measureType) {
        return new Measurand(channelPrefix, name, measureType);
    }

    static Skip skip(int bytes) {
        return new Skip(bytes);
    }
}
