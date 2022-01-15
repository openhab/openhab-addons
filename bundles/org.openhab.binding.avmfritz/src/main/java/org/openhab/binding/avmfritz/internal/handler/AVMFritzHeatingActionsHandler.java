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
package org.openhab.binding.avmfritz.internal.handler;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.avmfritz.internal.actions.AVMFritzHeatingActions;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

/**
 * The {@link AVMFritzHeatingActionsHandler} defines interface handlers to handle heating thing actions.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public interface AVMFritzHeatingActionsHandler extends ThingHandler {

    @Override
    default Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(AVMFritzHeatingActions.class);
    }

    /**
     * Activates the "Boost" mode of the heating thermostat or heating group.
     *
     * @param duration Duration in seconds, min. 1, max. 86400, 0 for deactivation.
     */
    void setBoostMode(long duration);

    /**
     * Activates the "Window Open" mode of the heating thermostat or heating group.
     *
     * @param duration Duration in seconds, min. 1, max. 86400, 0 for deactivation.
     */
    void setWindowOpenMode(long duration);
}
