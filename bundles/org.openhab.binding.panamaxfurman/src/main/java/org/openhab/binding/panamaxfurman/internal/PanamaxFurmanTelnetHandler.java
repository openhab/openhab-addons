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
package org.openhab.binding.panamaxfurman.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.panamaxfurman.internal.transport.PanamaxFurmanStreamTelnetTransport;
import org.openhab.binding.panamaxfurman.internal.transport.PanamaxFurmanTransport;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Contains fields mapping thing configuration parameters.
 *
 * @author Dave Badia - Initial contribution
 */
@NonNullByDefault
public class PanamaxFurmanTelnetHandler extends PanamaxFurmanAbstractHandler implements ThingHandler {

    public PanamaxFurmanTelnetHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected PanamaxFurmanTransport createTransport(Configuration genericConfig) {
        PanamaxFurmanTelnetConfiguration config = genericConfig.as(PanamaxFurmanTelnetConfiguration.class);
        return new PanamaxFurmanStreamTelnetTransport(config.getAddress(), config.getPort());
    }
}
