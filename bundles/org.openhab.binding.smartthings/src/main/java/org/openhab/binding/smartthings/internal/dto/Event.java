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
package org.openhab.binding.smartthings.internal.dto;

/**
 * Data object for smartthings Event
 *
 * @author Laurent ARNAL - Initial contribution
 */
public class Event {
    public String eventTime;
    public String evnetType;

    public class DeviceEvent {
        public String eventId;
        public String locationId;
        public String ownerId;
        public String ownerType;
        public String deviceId;
        public String componentId;
        public String capability;
        public String attribute;
        public String value;
        public String valueType;
        public Boolean stateChange;
        public String subscriptionName;
    }

    public DeviceEvent deviceEvent;
}
