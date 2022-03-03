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
package org.openhab.binding.netatmo.internal.handler.capability;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.VENDOR;
import static org.openhab.core.thing.Thing.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.dto.NADevice;
import org.openhab.binding.netatmo.internal.api.dto.NAEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeData;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus.HomeStatus;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.NAMain;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.handler.NACommonInterface;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
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
    protected final ModuleType moduleType;
    protected final String handlerId;
    protected final NACommonInterface handler;

    protected boolean firstLaunch;
    protected Map<String, String> properties = Map.of();
    protected ThingStatus thingStatus = ThingStatus.UNKNOWN;
    protected @Nullable String thingStatusReason = "";

    public Capability(NACommonInterface handler) {
        this.handler = handler;
        this.handlerId = handler.getId();
        this.thing = handler.getThing();
        this.moduleType = ModuleType.valueOf(thing.getThingTypeUID().getId());
    }

    // Data consumer functions
    public final void setNewData(@Nullable NAObject newData) {
        if (newData != null) {
            beforeNewData();
            if (newData instanceof NAHomeData) {
                updateHomeData((NAHomeData) newData);
            }
            if (newData instanceof HomeStatus) {
                updateHomeStatus((HomeStatus) newData);
            }
            if (newData instanceof NAHomeStatusModule) {
                updateHomeStatusModule((NAHomeStatusModule) newData);
            }
            if (newData instanceof NAEvent) {
                updateEvent((NAEvent) newData);
            }
            if (newData instanceof NAHomeEvent) {
                updateHomeEvent((NAHomeEvent) newData);
            }
            if (newData instanceof NAThing) {
                updateNAThing((NAThing) newData);
            }
            if (newData instanceof NAMain) {
                updateNAMain((NAMain) newData);
            }
            if (newData instanceof NADevice) {
                updateNADevice((NADevice) newData);
            }
            afterNewData(newData);
        }
    }

    protected void beforeNewData() {
        properties = new HashMap<>(thing.getProperties());
        firstLaunch = properties.isEmpty();
        if (firstLaunch && !moduleType.isLogical()) {
            properties.put(PROPERTY_VENDOR, VENDOR);
            properties.put(PROPERTY_MODEL_ID, moduleType.name());
        }
        thingStatus = ThingStatus.ONLINE;
        thingStatusReason = null;
    }

    protected void afterNewData(@Nullable NAObject newData) {
        if (!properties.equals(thing.getProperties())) {
            thing.setProperties(properties);
        }
        handler.setThingStatus(thingStatus, thingStatusReason);
    }

    protected void updateNAThing(NAThing newData) {
        String firmware = newData.getFirmware();
        if (firmware != null && !firmware.isBlank()) {
            properties.put(PROPERTY_FIRMWARE_VERSION, firmware);
        }
    }

    protected void updateNAMain(NAMain newData) {
        // do nothing by default, can be overridden by subclasses
    }

    protected void updateHomeEvent(NAHomeEvent newData) {
        // do nothing by default, can be overridden by subclasses
    }

    protected void updateHomeStatus(HomeStatus newData) {
        // do nothing by default, can be overridden by subclasses
    }

    protected void updateHomeData(NAHomeData newData) {
        // do nothing by default, can be overridden by subclasses
    }

    protected void updateEvent(NAEvent newData) {
        // do nothing by default, can be overridden by subclasses
    }

    protected void updateNADevice(NADevice newData) {
        // do nothing by default, can be overridden by subclasses
    }

    public void initialize() {
        // do nothing by default, can be overridden by subclasses
    }

    public void expireData() {
        // do nothing by default, can be overridden by subclasses
    }

    public void dispose() {
        // do nothing by default, can be overridden by subclasses
    }

    public void updateHomeStatusModule(NAHomeStatusModule newData) {
        // TODO Auto-generated method stub
    }

    // Command handling capability
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
