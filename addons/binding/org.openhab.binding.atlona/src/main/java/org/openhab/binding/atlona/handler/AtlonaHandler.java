/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.atlona.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;

/**
 * This abstract class should be the base class for any Atlona product line handler.
 *
 * @author Tim Roberts
 */
public abstract class AtlonaHandler<C extends AtlonaCapabilities> extends BaseThingHandler {

    /**
     * The model specific capabilities
     */
    private final C _capabilities;

    /**
     * Constructs the handler from the specified thing and capabilities
     *
     * @param thing a non-null {@link org.eclipse.smarthome.core.thing.Thing}
     * @param capabilities a non-null {@link org.openhab.binding.atlona.handler.AtlonaCapabilities}
     */
    public AtlonaHandler(Thing thing, C capabilities) {
        super(thing);

        if (capabilities == null) {
            throw new IllegalArgumentException("capabilities cannot be null");
        }
        _capabilities = capabilities;
    }

    /**
     * Returns the model specific capabilities
     *
     * @return a non-null {@link org.openhab.binding.atlona.handler.AtlonaCapabilities}
     */
    protected C getCapabilities() {
        return _capabilities;
    }
}
