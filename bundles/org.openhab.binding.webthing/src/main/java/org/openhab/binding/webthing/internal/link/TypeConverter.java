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
package org.openhab.binding.webthing.internal.link;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link TypeConverter} class map Item state <-> Property value
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
interface TypeConverter {

    /**
     * * maps a Property value to an Item state command
     * 
     * @param propertyValue the Property value
     * @return the Item state command
     */
    Command toStateCommand(Object propertyValue);

    /**
     * maps an Item state to a Property value
     * 
     * @param state the Item state
     * @return the Property value
     */
    Object toPropertyValue(State state);
}
