/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.lcn.internal.converter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.types.State;

/**
 * Interface for all converters.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public interface Converter {
    /**
     * Converts a state update from the Thing into a human readable representational State.
     *
     * @param state from the Thing
     * @return human readable representational State
     */
    public State onStateUpdateFromHandler(State state);
}
