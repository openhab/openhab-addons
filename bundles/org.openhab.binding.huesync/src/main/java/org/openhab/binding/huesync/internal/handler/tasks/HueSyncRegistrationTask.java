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
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDeviceDto;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistrationDto;
import org.openhab.binding.huesync.internal.connection.HueSyncDeviceConnection;
import org.openhab.binding.huesync.internal.log.HueSyncLogFactory;
import org.slf4j.Logger;

/**
 * Task to handle device registration.
 * 
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class HueSyncRegistrationTask implements Runnable {
    private final Logger logger = HueSyncLogFactory.getLogger(HueSyncRegistrationTask.class);

    private HueSyncDeviceConnection connection;
    private HueSyncDeviceDto deviceInfo;

    private Consumer<HueSyncRegistrationDto> action;

    public HueSyncRegistrationTask(HueSyncDeviceConnection connection, HueSyncDeviceDto deviceInfo,
            Consumer<HueSyncRegistrationDto> action) {

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

            this.logger.info("Listening for device registration - {} {}:{}", this.deviceInfo.name,
                    this.deviceInfo.deviceType, id);

            HueSyncRegistrationDto registration = this.connection.registerDevice(id);

            if (registration != null) {
                this.logger.info("API token for {} received", this.deviceInfo.name);

                this.action.accept(registration);
            }
        } catch (Exception e) {
            this.logger.debug("{}", e.getMessage());
        }
    }
}
