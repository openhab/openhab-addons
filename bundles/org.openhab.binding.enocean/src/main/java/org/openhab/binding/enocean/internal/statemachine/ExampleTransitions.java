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

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Example transition configuration demonstrating state machine infrastructure usage.
 * <p>
 * This minimal example shows how to:
 * <ul>
 * <li>Define state transitions between custom states</li>
 * <li>Trigger actions that cause state changes</li>
 * <li>Create a simple three-state workflow: IDLE → WARMUP → ACTIVE → IDLE</li>
 * </ul>
 * <p>
 * Usage example:
 * 
 * <pre>
 * STMStateMachine&lt;ExampleAction, ExampleState&gt; stm = STMStateMachine.build(ExampleTransitions.SIMPLE,
 *         ExampleState.IDLE, scheduler, this::onStateChanged);
 *
 * stm.apply(ExampleAction.START); // IDLE → WARMUP
 * stm.apply(ExampleAction.READY); // WARMUP → ACTIVE
 * stm.apply(ExampleAction.FINISH); // ACTIVE → IDLE
 * </pre>
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class ExampleTransitions {

    /**
     * Simple three-state workflow demonstrating basic state machine functionality.
     */
    public static final STMTransitionConfiguration<ExampleAction, ExampleState> SIMPLE = new STMTransitionConfiguration<>(
            Arrays.asList(
                    // IDLE → WARMUP when START action occurs
                    new STMTransition<>(ExampleState.IDLE, ExampleAction.START, ExampleState.WARMUP),
                    // WARMUP → ACTIVE when READY action occurs
                    new STMTransition<>(ExampleState.WARMUP, ExampleAction.READY, ExampleState.ACTIVE),
                    // ACTIVE → IDLE when FINISH action occurs
                    new STMTransition<>(ExampleState.ACTIVE, ExampleAction.FINISH, ExampleState.IDLE)));
}
