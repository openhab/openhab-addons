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
package org.openhab.binding.atv.internal.client.scan;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.conf.BaseService;
import org.openhab.binding.atv.internal.client.conf.Service;
import org.openhab.binding.atv.internal.client.dto.DeviceInfo;
import org.openhab.binding.atv.internal.client.dto.Protocol;

/**
 * Updates a discovered service with additional information, e.g. the pairing
 * requirement, once all services of a device are known.
 *
 * @author Dan Cunningham - Initial contribution
 */
@FunctionalInterface
@NonNullByDefault
public interface ServiceInfoMethod {

    /**
     * Updates the service.
     *
     * @param service service to update
     * @param deviceInfo merged device information of the device
     * @param services all services of the device, keyed by protocol
     */
    void update(Service service, DeviceInfo deviceInfo, Map<Protocol, BaseService> services);
}
