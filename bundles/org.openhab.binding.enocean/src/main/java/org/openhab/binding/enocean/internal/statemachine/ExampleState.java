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
 * Example states for demonstrating state machine infrastructure.
 * This is a minimal example showing how to define custom states for a state machine.
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public enum ExampleState {
    IDLE, // Ready to start
    WARMUP, // Preparing
    ACTIVE // Working
}
