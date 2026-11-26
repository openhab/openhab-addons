/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler.capability;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.dto.NAError;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;

/**
 * The {@link Capability} is the base class for all inherited capabilities
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class Capability {
    protected final CommonInterface handler;
    protected final ModuleType moduleType;
    protected final ThingUID thingUID;

    protected boolean firstLaunch = true;
    protected Map<String, String> properties = Map.of();
    protected @Nullable String statusReason;

    Capability(CommonInterface handler) {
        this.handler = handler;
        this.thingUID = getThing().getUID();
        this.moduleType = ModuleType.from(getThing().getThingTypeUID());
    }

    public final @Nullable String setNewData(Object newData) {
        beforeNewData();
        if (newData instanceof NAError error) {
            updateErrors(error);
        } else {
            internalSetNewData(newData);
        }
        afterNewData(newData);
        return statusReason;
    }

    protected void beforeNewData() {
        properties = new HashMap<>(getThing().getProperties());
        statusReason = null;
    }

    protected void internalSetNewData(Object newData) {
        // do nothing by default, can be overridden by subclasses
    }

    protected void afterNewData(Object newData) {
        if (!properties.equals(getThing().getProperties())) {
            getThing().setProperties(properties);
        }
        firstLaunch = false;
    }

    protected void updateErrors(NAError error) {
        // do nothing by default, can be overridden by subclasses
    }

    public void initialize() {
        // do nothing by default, can be overridden by subclasses
    }

    public void expireData() {
        CommonInterface bridgeHandler = handler.getBridgeHandler();
        if (bridgeHandler != null && handler.getCapabilities().getOrDescendant(RefreshCapability.class).isEmpty()) {
            bridgeHandler.expireData();
        }
    }

    public void dispose() {
        // do nothing by default, can be overridden by subclasses
    }

    public void handleCommand(String channelName, Command command) {
        // do nothing by default, can be overridden by subclasses
    }

    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of();
    }

    public List<NAObject> updateReadings() {
        return List.of();
    }

    protected Thing getThing() {
        return handler.getThing();
    }
}
