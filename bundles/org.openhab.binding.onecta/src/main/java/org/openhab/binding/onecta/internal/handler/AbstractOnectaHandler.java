/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.onecta.internal.handler;

import java.util.Optional;

import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;

/**
 * The {@link AbstractOnectaHandler} abstract for all Onecta Handlers
 *
 * @author Alexander Drent - Initial contribution
 */
public abstract class AbstractOnectaHandler extends BaseThingHandler {

    public AbstractOnectaHandler(Thing thing) {
        super(thing);
    }

    public abstract void refreshDevice();

    public String getUnitID() {
        return Optional.ofNullable(thing.getConfiguration().get("unitID")).orElse("").toString();
    }
}
