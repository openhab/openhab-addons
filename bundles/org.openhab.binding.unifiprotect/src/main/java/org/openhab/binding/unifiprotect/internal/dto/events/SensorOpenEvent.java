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
package org.openhab.binding.unifiprotect.internal.dto.events;

import org.openhab.binding.unifiprotect.internal.dto.SensorMountType;

/**
 * Sensor opened event.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class SensorOpenEvent extends BaseEvent {
    public Metadata metadata;

    public static class Metadata {
        public SensorMountTypeWrapper sensorMountType;
    }

    public static class SensorMountTypeWrapper {
        public SensorMountType text;
    }
}
