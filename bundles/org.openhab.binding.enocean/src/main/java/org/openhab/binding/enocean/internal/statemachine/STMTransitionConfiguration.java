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
package org.openhab.binding.enocean.internal.statemachine;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Transition configuration for state machines.
 * <p>
 * Contains the list of valid transitions for a specific device type.
 *
 * @param <A> the action type (enum)
 * @param <S> the state type (enum)
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class STMTransitionConfiguration<A extends Enum<A>, S extends Enum<S>> {

    private final List<STMTransition<A, S>> transitions;

    public STMTransitionConfiguration(List<STMTransition<A, S>> transitions) {
        this.transitions = new ArrayList<>(transitions);
    }

    public List<STMTransition<A, S>> getTransitions() {
        return transitions;
    }
}
