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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Supervises states in which a state machine waits for feedback from the device.
 *
 * @param <A> the action type (enum)
 * @param <S> the state type (enum)
 * @param states the states to supervise
 * @param timeoutAction the action applied when a supervised state is not left in time
 * @param timeoutMs the maximum time in milliseconds a supervised state may last
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public record STMWatchdog<A extends Enum<A>, S extends Enum<S>> (Set<S> states, A timeoutAction, long timeoutMs) {
}
