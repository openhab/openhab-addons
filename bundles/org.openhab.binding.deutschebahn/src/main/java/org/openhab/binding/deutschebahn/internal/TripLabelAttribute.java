/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.deutschebahn.internal;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TimetableStop;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TripLabel;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TripType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Selection that returns the value of an {@link TripLabel}.
 * 
 * chapter "1.2.7 TripLabel" in Technical Interface Description for external Developers
 *
 * @see https://developer.deutschebahn.com/store/apis/info?name=Timetables&version=v1&provider=DBOpenData&#tab1
 * 
 * @author Sönke Küper - Initial contribution.
 * 
 * @param <VALUE_TYPE> type of value in Bean.
 * @param <STATE_TYPE> type of state.
 */
@NonNullByDefault
public final class TripLabelAttribute<VALUE_TYPE, STATE_TYPE extends State>
        extends AbstractDtoAttributeSelector<TripLabel, VALUE_TYPE, STATE_TYPE> implements AttributeSelection {

    /**
     * Trip category.
     */
    public static final TripLabelAttribute<String, StringType> C = new TripLabelAttribute<>("category", TripLabel::getC,
            TripLabel::setC, StringType::new, StringType.class);

    /**
     * Number.
     */
    public static final TripLabelAttribute<String, StringType> N = new TripLabelAttribute<>("number", TripLabel::getN,
            TripLabel::setN, StringType::new, StringType.class);

    /**
     * Filter flags.
     */
    public static final TripLabelAttribute<String, StringType> F = new TripLabelAttribute<>("filter-flags",
            TripLabel::getF, TripLabel::setF, StringType::new, StringType.class);
    /**
     * Trip Type.
     */
    public static final TripLabelAttribute<TripType, StringType> T = new TripLabelAttribute<>("trip-type",
            TripLabel::getT, TripLabel::setT, TripLabelAttribute::fromTripType, StringType.class);
    /**
     * Owner.
     */
    public static final TripLabelAttribute<String, StringType> O = new TripLabelAttribute<>("owner", TripLabel::getO,
            TripLabel::setO, StringType::new, StringType.class);

    /**
     * Creates an new {@link TripLabelAttribute}.
     *
     * @param getter Function to get the raw value.
     * @param setter Function to set the raw value.
     * @param getState Function to get the Value as {@link State}.
     */
    private TripLabelAttribute(final String channelTypeName, //
            final Function<TripLabel, @Nullable VALUE_TYPE> getter, //
            final BiConsumer<TripLabel, VALUE_TYPE> setter, //
            final Function<VALUE_TYPE, @Nullable STATE_TYPE> getState, //
            final Class<STATE_TYPE> stateType) {
        super(channelTypeName, getter, setter, getState, stateType);
    }

    @Nullable
    @Override
    public State getState(TimetableStop stop) {
        if (stop.getTl() == null) {
            return UnDefType.UNDEF;
        }
        return super.getState(stop.getTl());
    }

    private static StringType fromTripType(final TripType value) {
        return new StringType(value.value());
    }

    /**
     * Returns an {@link TripLabelAttribute} for the given channel-name.
     */
    @Nullable
    public static TripLabelAttribute<?, ?> getByChannelName(final String channelName) {
        switch (channelName) {
            case "category":
                return C;
            case "number":
                return N;
            case "filter-flags":
                return F;
            case "trip-type":
                return T;
            case "owner":
                return O;
            default:
                return null;
        }
    }
}
