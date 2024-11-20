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
package org.openhab.binding.huesync.internal.handler.tasks;

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDevice;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistration;
import org.openhab.binding.huesync.internal.connection.HueSyncDeviceConnection;
import org.openhab.binding.huesync.internal.exceptions.HueSyncConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task to handle device registration.
 * 
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class HueSyncRegistrationTask implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(HueSyncRegistrationTask.class);

    private final HueSyncDeviceConnection connection;
    private final HueSyncDevice deviceInfo;
    private final Consumer<HueSyncRegistration> action;

    public HueSyncRegistrationTask(HueSyncDeviceConnection connection, HueSyncDevice deviceInfo,
            Consumer<HueSyncRegistration> action) {
        this.connection = connection;
        this.deviceInfo = deviceInfo;
        this.action = action;
    }

    @Override
    public void run() {
        try {
            String id = this.deviceInfo.uniqueId;

            if (this.connection.isRegistered() || id == null) {
                return;
            }

            this.logger.debug("Listening for device registration - {} {}:{}", this.deviceInfo.name,
                    this.deviceInfo.deviceType, id);

            HueSyncRegistration registration = this.connection.registerDevice(id);

            if (registration != null) {
                this.logger.debug("API token for {} received", this.deviceInfo.name);

                this.action.accept(registration);
            }
        } catch (HueSyncConnectionException e) {
            this.logger.warn("{}", e.getMessage());
        }
    }
}
