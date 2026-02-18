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
package org.openhab.binding.enocean.internal.eep;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.statemachine.STMTransitionConfiguration;
import org.openhab.core.thing.Thing;

/**
 * Interface for EEPs that provide a state machine for operation.
 * <p>
 * EEPs implementing this interface provide their state machine configuration
 * and channel initialization logic, allowing the handler to remain generic.
 *
 * @param <A> the action type (enum)
 * @param <S> the state type (enum)
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public interface StateMachineProvider<A extends Enum<A>, S extends Enum<S>> {

    /**
     * Gets the transition configuration for the state machine.
     * The implementation may read device-specific configuration from the Thing.
     *
     * @param thing the Thing to read configuration from
     * @return the transition configuration, or null if STM should not be used (e.g., legacy mode)
     */
    @Nullable
    STMTransitionConfiguration<A, S> getTransitionConfiguration(Thing thing);

    /**
     * Gets the initial state for the state machine.
     *
     * @return the initial state
     */
    S getInitialState();

    /**
     * Gets the actions that require callback registration.
     * These actions will trigger processStoredCommand when completed.
     *
     * @param thing the Thing to read configuration from
     * @return set of actions requiring callbacks
     */
    Set<A> getRequiredCallbackActions(Thing thing);

    /**
     * Gets the channel IDs that should be removed based on the device configuration.
     *
     * @param thing the Thing to read configuration from
     * @return set of channel IDs to remove (empty if none should be removed)
     */
    Set<String> getChannelsToRemove(Thing thing);
}
