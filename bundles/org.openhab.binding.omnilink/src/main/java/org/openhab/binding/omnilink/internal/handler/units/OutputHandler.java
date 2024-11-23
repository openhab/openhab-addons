/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.omnilink.internal.handler.units;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.handler.UnitHandler;
import org.openhab.core.thing.Thing;

/**
 * The {@link OutputHandler} defines some methods that are used to
 * interface with an OmniLink Output. This by extension also defines the
 * Output thing that openHAB will be able to pick up and interface with.
 *
 * @author Brian O'Connell - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
public class OutputHandler extends UnitHandler {
    public @Nullable String number;

    public OutputHandler(Thing thing) {
        super(thing);
    }
}
