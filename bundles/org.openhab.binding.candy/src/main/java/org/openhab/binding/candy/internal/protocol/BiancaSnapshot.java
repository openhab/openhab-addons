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
package org.openhab.binding.candy.internal.protocol;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.State;

/**
 * Immutable channel values from one local response, without retaining the raw device data.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public record BiancaSnapshot(Map<String, State> states) {
    public BiancaSnapshot {
        states = Map.copyOf(states);
    }
}
