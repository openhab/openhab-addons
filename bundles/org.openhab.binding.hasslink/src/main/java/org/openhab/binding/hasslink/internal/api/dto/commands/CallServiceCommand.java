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
package org.openhab.binding.hasslink.internal.api.dto.commands;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link CallServiceCommand} dispatches a Home Assistant service call via WebSocket.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class CallServiceCommand extends BaseCommand {

    public final String domain;
    public final String service;
    public final @Nullable ServiceTarget target;
    public final @Nullable Map<String, Object> serviceData;

    /**
     * Call service with no target (system/global services).
     */
    public CallServiceCommand(int id, String domain, String service, @Nullable Map<String, Object> serviceData) {
        this(id, domain, service, (ServiceTarget) null, serviceData);
    }

    /**
     * Call service with a single entity target helper.
     */
    public CallServiceCommand(int id, String domain, String service, String entityId,
            @Nullable Map<String, Object> serviceData) {
        this(id, domain, service, ServiceTarget.forEntity(entityId), serviceData);
    }

    /**
     * Call service with a full ServiceTarget object.
     */
    public CallServiceCommand(int id, String domain, String service, @Nullable ServiceTarget target,
            @Nullable Map<String, Object> serviceData) {
        super(id, "call_service");
        this.domain = domain;
        this.service = service;
        this.target = target;
        this.serviceData = serviceData;
    }

    /**
     * Represents the Home Assistant service target selector dictionary.
     */
    public static class ServiceTarget {

        public final @Nullable Object entityId; // String or List<String>

        public final @Nullable Object deviceId; // String or List<String>

        public final @Nullable Object areaId; // String or List<String>

        public final @Nullable Object floorId; // String or List<String>

        public final @Nullable Object labelId; // String or List<String>

        public ServiceTarget(@Nullable Object entityId, @Nullable Object deviceId, @Nullable Object areaId,
                @Nullable Object floorId, @Nullable Object labelId) {
            this.entityId = entityId;
            this.deviceId = deviceId;
            this.areaId = areaId;
            this.floorId = floorId;
            this.labelId = labelId;
        }

        public static ServiceTarget forEntity(String entityId) {
            return new ServiceTarget(entityId, null, null, null, null);
        }

        public static ServiceTarget forDevice(String deviceId) {
            return new ServiceTarget(null, deviceId, null, null, null);
        }

        public static ServiceTarget forArea(String areaId) {
            return new ServiceTarget(null, null, areaId, null, null);
        }
    }
}
