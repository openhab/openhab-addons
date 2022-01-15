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
package org.openhab.binding.atlona.internal.handler;

import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;

/**
 * This abstract class should be the base class for any Atlona product line handler.
 *
 * @author Tim Roberts - Initial contribution
 */
public abstract class AtlonaHandler<C extends AtlonaCapabilities> extends BaseThingHandler {

    /**
     * The model specific capabilities
     */
    private final C capabilities;

    /**
     * Constructs the handler from the specified thing and capabilities
     *
     * @param thing a non-null {@link org.openhab.core.thing.Thing}
     * @param capabilities a non-null {@link org.openhab.binding.atlona.internal.handler.AtlonaCapabilities}
     */
    public AtlonaHandler(Thing thing, C capabilities) {
        super(thing);

        if (capabilities == null) {
            throw new IllegalArgumentException("capabilities cannot be null");
        }
        this.capabilities = capabilities;
    }

    /**
     * Returns the model specific capabilities
     *
     * @return a non-null {@link org.openhab.binding.atlona.internal.handler.AtlonaCapabilities}
     */
    protected C getCapabilities() {
        return capabilities;
    }
}
