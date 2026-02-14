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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents a single transition in the state machine.
 *
 * @param <A> the action type (enum)
 * @param <S> the state type (enum)
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class STMTransition<A extends Enum<A>, S extends Enum<S>> {
    S from;
    A action;
    S to;

    public STMTransition(S from, A action, S to) {
        this.from = from;
        this.action = action;
        this.to = to;
    }
}
