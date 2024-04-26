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
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDeviceInfo;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistration;
import org.openhab.binding.huesync.internal.connection.HueSyncDeviceConnection;
import org.openhab.binding.huesync.internal.log.HueSyncLogFactory;
import org.openhab.core.thing.ThingStatus;
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
    private HueSyncDeviceInfo deviceInfo;

    private Consumer<HueSyncRegistration> action;
    private Supplier<ThingStatus> status;

    public HueSyncRegistrationTask(HueSyncDeviceConnection connection, HueSyncDeviceInfo deviceInfo,
            Supplier<ThingStatus> status, Consumer<HueSyncRegistration> action) {

        this.connection = connection;
        this.deviceInfo = deviceInfo;
        this.status = status;
        this.action = action;
    }

    @Override
    public void run() {
        try {
            if (this.status.get() == ThingStatus.OFFLINE) {
                HueSyncRegistration registration = this.connection.registerDevice(deviceInfo.uniqueId);

                if (registration != null) {
                    this.action.accept(registration);
                }
            }
        } catch (Exception e) {
            this.logger.debug("{}", e.getMessage());
        }
    }
}
