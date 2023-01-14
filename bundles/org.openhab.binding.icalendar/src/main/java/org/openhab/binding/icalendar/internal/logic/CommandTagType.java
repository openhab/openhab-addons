/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.icalendar.internal.logic;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A type enumerator to indicate whether a Command Tag is of type BEGIN or END; as in the following examples:
 *
 * BEGIN:<item_name>:<new_state>
 * END:<item_name>:<new_state>
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum CommandTagType {
    BEGIN,
    END;

    public static boolean prefixValid(@Nullable String line) {
        return (line != null) && (line.startsWith(BEGIN.toString()) || line.startsWith(END.toString()));
    }
}
