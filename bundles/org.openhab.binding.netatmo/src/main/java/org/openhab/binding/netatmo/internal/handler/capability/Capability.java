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
package org.openhab.binding.netatmo.internal.handler.capability;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.dto.Device;
import org.openhab.binding.netatmo.internal.api.dto.Event;
import org.openhab.binding.netatmo.internal.api.dto.HomeData;
import org.openhab.binding.netatmo.internal.api.dto.HomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.NAError;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus.HomeStatus;
import org.openhab.binding.netatmo.internal.api.dto.NAMain;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.api.dto.WebhookEvent;
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
    protected final Thing thing;
    protected final CommonInterface handler;
    protected final ModuleType moduleType;
    protected final ThingUID thingUID;

    protected boolean firstLaunch = true;
    protected Map<String, String> properties = Map.of();
    protected @Nullable String statusReason;

    Capability(CommonInterface handler) {
        this.handler = handler;
        this.thing = handler.getThing();
        this.thingUID = thing.getUID();
        this.moduleType = ModuleType.from(thing.getThingTypeUID());
    }

    public final @Nullable String setNewData(NAObject newData) {
        beforeNewData();
        if (newData instanceof NAError error) {
            updateErrors(error);
        } else {
            if (newData instanceof HomeData homeData) {
                updateHomeData(homeData);
            }
            if (newData instanceof HomeStatus homeStatus) {
                updateHomeStatus(homeStatus);
            }
            if (newData instanceof HomeStatusModule homeStatusModule) {
                updateHomeStatusModule(homeStatusModule);
            }

            if (newData instanceof HomeEvent homeEvent) {
                updateHomeEvent(homeEvent);
            } else if (newData instanceof WebhookEvent webhookEvent
                    && webhookEvent.getEventType().validFor(moduleType)) {
                updateWebhookEvent(webhookEvent);
            } else if (newData instanceof Event event) {
                updateEvent(event);
            }

            if (newData instanceof NAThing naThing) {
                updateNAThing(naThing);
            }
            if (newData instanceof NAMain naMain) {
                updateNAMain(naMain);
            }
            if (newData instanceof Device device) {
                updateNADevice(device);
            }
        }
        afterNewData(newData);
        return statusReason;
    }

    protected void beforeNewData() {
        properties = new HashMap<>(thing.getProperties());
        statusReason = null;
    }

    protected void afterNewData(@Nullable NAObject newData) {
        if (!properties.equals(thing.getProperties())) {
            thing.setProperties(properties);
        }
        firstLaunch = false;
    }

    protected void updateNAThing(NAThing newData) {
        // do nothing by default, can be overridden by subclasses
    }

    protected void updateNAMain(NAMain newData) {
        // do nothing by default, can be overridden by subclasses
    }

    protected void updateHomeEvent(HomeEvent newData) {
        // do nothing by default, can be overridden by subclasses
    }

    protected void updateHomeStatus(HomeStatus newData) {
        // do nothing by default, can be overridden by subclasses
    }

    protected void updateHomeData(HomeData newData) {
        // do nothing by default, can be overridden by subclasses
    }

    protected void updateEvent(Event newData) {
        // do nothing by default, can be overridden by subclasses
    }

    protected void updateWebhookEvent(WebhookEvent newData) {
        // do nothing by default, can be overridden by subclasses
    }

    protected void updateNADevice(Device newData) {
        // do nothing by default, can be overridden by subclasses
    }

    protected void updateErrors(NAError error) {
        // do nothing by default, can be overridden by subclasses
    }

    public void initialize() {
        // do nothing by default, can be overridden by subclasses
    }

    public void expireData() {
        CommonInterface bridgeHandler = handler.getBridgeHandler();
        if (bridgeHandler != null && handler.getCapabilities().getRefresh().isEmpty()) {
            bridgeHandler.expireData();
        }
    }

    public void dispose() {
        // do nothing by default, can be overridden by subclasses
    }

    public void updateHomeStatusModule(HomeStatusModule newData) {
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
}
