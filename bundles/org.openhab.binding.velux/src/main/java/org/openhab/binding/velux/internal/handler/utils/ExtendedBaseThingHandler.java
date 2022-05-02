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
package org.openhab.binding.velux.internal.handler.utils;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;

/**
 * The {@link ExtendedBaseThingHandler} extended the {@link BaseThingHandler} interface and adds <B>publicly
 * visible</B> convenience methods for property handling.
 * <p>
 * It is recommended to extend this abstract base class.
 * <p>
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
public abstract class ExtendedBaseThingHandler extends BaseThingHandler {

    /*
     * ************************
     * ***** Constructors *****
     */

    /**
     * @see BaseThingHandler
     * @param thing which will be created.
     */
    protected ExtendedBaseThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * Returns a copy of the properties map, that can be modified. The method {@link #updateProperties} must be called
     * to persist the properties.
     *
     * @return copy of the thing properties (not null)
     */
    @Override
    public Map<String, String> editProperties() {
        return super.editProperties();
    }

    /**
     * Informs the framework, that the given properties map of the thing was updated. This method performs a check, if
     * the properties were updated. If the properties did not change, the framework is not informed about changes.
     *
     * @param properties properties map, that was updated and should be persisted
     */
    @Override
    public void updateProperties(@Nullable Map<String, String> properties) {
        super.updateProperties(properties);
    }
}
