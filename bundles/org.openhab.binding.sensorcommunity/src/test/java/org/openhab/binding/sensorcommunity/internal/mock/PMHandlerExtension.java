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
package org.openhab.binding.sensorcommunity.internal.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sensorcommunity.internal.handler.PMHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 * The {@link PMHandlerExtension} Test Particualte Matter Handler Extension with additonal state queries
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class PMHandlerExtension extends PMHandler {

    public PMHandlerExtension(Thing thing) {
        super(thing);
    }

    public ConfigStatus getConfigStatus() {
        return configStatus;
    }

    public UpdateStatus getUpdateStatus() {
        return lastUpdateStatus;
    }

    public @Nullable State getPM25Cache() {
        return pm25Cache;
    }

    public @Nullable State getPM100Cache() {
        return pm100Cache;
    }
}
