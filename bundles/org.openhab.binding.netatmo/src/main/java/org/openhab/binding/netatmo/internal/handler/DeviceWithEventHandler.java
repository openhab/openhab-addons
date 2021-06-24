/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.PROPERTY_MAX_EVENT_TIME;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.dto.NAEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.webhook.NetatmoServlet;
import org.openhab.core.thing.Bridge;

/**
 * {@link DeviceWithEventHandler} is the base class for handlers
 * subject to receive event notifications. This class registers to webhookservlet so
 * it can be notified when an event arrives.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public abstract class DeviceWithEventHandler extends DeviceHandler {

    class LastEventTimeHelper {
        private @Nullable ZonedDateTime maxEventTime;

        protected ZonedDateTime get() {
            ZonedDateTime eventTime = maxEventTime;
            if (eventTime == null) {
                String lastEvent = editProperties().get(PROPERTY_MAX_EVENT_TIME);
                eventTime = lastEvent != null ? ZonedDateTime.parse(lastEvent) : Instant.EPOCH.atZone(ZoneOffset.UTC);
                this.maxEventTime = eventTime;
            }
            return eventTime;
        }

        void set(ZonedDateTime maxEventTime) {
            this.maxEventTime = maxEventTime;
            updateProperty(PROPERTY_MAX_EVENT_TIME, maxEventTime.toString());
        }
    }

    private final LastEventTimeHelper lastEventTime = new LastEventTimeHelper();
    private @Nullable NetatmoServlet webhookServlet;

    public DeviceWithEventHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider) {
        super(bridge, channelHelpers, apiBridge, descriptionProvider);
    }

    @Override
    public void initialize() {
        super.initialize();
        NetatmoServlet servlet = this.webhookServlet;
        if (servlet != null) {
            servlet.registerDataListener(config.id, this);
        }
    }

    @Override
    public void dispose() {
        NetatmoServlet servlet = this.webhookServlet;
        if (servlet != null) {
            servlet.unregisterDataListener(this);
        }
        super.dispose();
    }

    @Override
    public void setNewData(NAObject newData) {
        if (newData instanceof NAEvent) {
            NAEvent event = (NAEvent) newData;
            if (event.getTime().isAfter(lastEventTime.get())) {
                lastEventTime.set(event.getTime());
                internalSetNewEvent(event);
            }
        }
        super.setNewData(newData);
    }

    protected abstract void internalSetNewEvent(NAEvent event);

    @Override
    protected void updateChildModules(NAObject newData) {
        if (newData instanceof NAEvent) {
            NAEvent event = (NAEvent) newData;
            DeviceHandler person = getDataListeners().get(event.getPersonId());
            if (person != null) {
                person.setNewData(event);
            }
            DeviceHandler camera = getDataListeners().get(event.getCameraId());
            if (camera != null) {
                camera.setNewData(event);

            }
        } else {
            super.updateChildModules(newData);
        }
    }

    public void setWebHookServlet(NetatmoServlet servlet) {
        this.webhookServlet = servlet;
    }
}
