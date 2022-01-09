/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deutschebahn.internal.timetable.dto.JaxbEntity;
import org.openhab.core.types.State;

/**
 * Accessor for attribute value of an DTO-Object.
 * 
 * @author Sönke Küper - Initial contribution.
 *
 * @param <DTO_TYPE> type of value in Bean.
 * @param <VALUE_TYPE> type of value in Bean.
 * @param <STATE_TYPE> type of state.
 */
@NonNullByDefault
public abstract class AbstractDtoAttributeSelector<DTO_TYPE extends JaxbEntity, @Nullable VALUE_TYPE, STATE_TYPE extends State> {

    private final Function<DTO_TYPE, @Nullable VALUE_TYPE> getter;
    private final BiConsumer<DTO_TYPE, VALUE_TYPE> setter;
    private final Function<VALUE_TYPE, @Nullable STATE_TYPE> getState;
    private final String channelTypeName;
    private final Class<STATE_TYPE> stateType;
    private final Function<VALUE_TYPE, List<String>> valueToList;

    /**
     * Creates an new {@link EventAttribute}.
     *
     * @param getter Function to get the raw value.
     * @param setter Function to set the raw value.
     * @param getState Function to get the Value as {@link State}.
     */
    protected AbstractDtoAttributeSelector(final String channelTypeName, //
            final Function<DTO_TYPE, @Nullable VALUE_TYPE> getter, //
            final BiConsumer<DTO_TYPE, VALUE_TYPE> setter, //
            final Function<VALUE_TYPE, @Nullable STATE_TYPE> getState, //
            final Function<VALUE_TYPE, List<String>> valueToList, //
            final Class<STATE_TYPE> stateType) {
        this.channelTypeName = channelTypeName;
        this.getter = getter;
        this.setter = setter;
        this.getState = getState;
        this.valueToList = valueToList;
        this.stateType = stateType;
    }

    /**
     * Returns the type of the state value.
     */
    public final Class<STATE_TYPE> getStateType() {
        return this.stateType;
    }

    /**
     * Returns the name of the corresponding channel-type.
     */
    public final String getChannelTypeName() {
        return this.channelTypeName;
    }

    /**
     * Returns the {@link State} for the selected attribute from the given DTO object
     * Returns <code>null</code> if the value is <code>null</code>.
     */
    @Nullable
    public final STATE_TYPE getState(final DTO_TYPE object) {
        final VALUE_TYPE value = this.getValue(object);
        if (value == null) {
            return null;
        }
        return this.getState.apply(value);
    }

    /**
     * Returns the value for the selected attribute from the given DTO object.
     */
    @Nullable
    public final VALUE_TYPE getValue(final DTO_TYPE object) {
        return this.getter.apply(object);
    }

    /**
     * Returns a list of values as string list.
     * Returns empty list if value is not present, singleton list if attribute is not single-valued.
     */
    public final List<String> getStringValues(DTO_TYPE object) {
        return this.valueToList.apply(getValue(object));
    }

    /**
     * Sets the value for the selected attribute in the given DTO object
     */
    public final void setValue(final DTO_TYPE event, final VALUE_TYPE object) {
        this.setter.accept(event, object);
    }
}
