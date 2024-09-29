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
package org.openhab.binding.miele.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miele.internal.MieleTranslationProvider;
import org.openhab.binding.miele.internal.api.dto.DeviceMetaData;
import org.openhab.core.types.State;

/**
 * The {@link ApplianceChannelSelector} class defines a common interface for
 * all the data structures used by appliance thing handlers. It is used to traverse
 * the channels that possibly exist for an appliance, and convert data
 * returned by the appliance to a compatible State
 *
 * @author Karel Goderis - Initial contribution
 * @author Jacob Laursen - Added power/water consumption channels
 */
@NonNullByDefault
public interface ApplianceChannelSelector {

    @Override
    String toString();

    /**
     * Returns the ChannelID for the given datapoint
     */
    String getChannelID();

    /**
     * Returns the Miele defined ID for the given datapoint
     */
    String getMieleID();

    /**
     * Returns true if the given datapoint is to be considered as a Property
     * instead of a regular modifiable datapoint
     */
    boolean isProperty();

    /**
     * Returns true if the given channel is extracted from extended
     * state information
     */
    boolean isExtendedState();

    /**
     * Returns a State for the given string, taking into
     * account the metadata provided as well as text
     * translations for corresponding numeric values.
     *
     * @param s - the value to be used to instantiate the State
     * @param dmd - the device meta data
     * @param translationProvider {@link MieleTranslationProvider} instance
     */
    State getState(String s, @Nullable DeviceMetaData dmd, MieleTranslationProvider translationProvider);

    /**
     * Returns a State for the given string, taking into
     * account the metadata provided. The meta data is sent by
     * the Miele appliance and is used to decide the State type
     *
     * @param s - the value to be used to instantiate the State
     * @param dmd - the device meta data
     */
    State getState(String s, @Nullable DeviceMetaData dmd);

    /**
     * Returns a raw State for the given string, not taking into
     * account any metadata.
     *
     * @param s - the value to be used to instantiate the State
     */
    State getState(String s);
}
