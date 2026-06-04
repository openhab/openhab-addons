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
package org.openhab.binding.matter.internal.controller.devices.types;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BooleanStateCluster;
import org.openhab.binding.matter.internal.controller.devices.converter.ContactStateConverter;
import org.openhab.binding.matter.internal.controller.devices.converter.GenericConverter;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;

/**
 * A DeviceType for contact sensors.
 *
 * Contact sensors use BooleanState where TRUE means CLOSED and FALSE means OPEN.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class ContactSensorType extends DeviceType {

    public ContactSensorType(Integer deviceType, MatterBaseThingHandler handler, Integer endpointNumber) {
        super(deviceType, handler, endpointNumber);
    }

    @Override
    protected @Nullable GenericConverter<? extends BaseCluster> createConverter(BaseCluster cluster,
            Map<String, BaseCluster> allClusters, String labelPrefix) {
        if (cluster instanceof BooleanStateCluster booleanStateCluster) {
            return new ContactStateConverter(booleanStateCluster, handler, endpointNumber, labelPrefix);
        }

        return super.createConverter(cluster, allClusters, labelPrefix);
    }
}
