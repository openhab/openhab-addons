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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDevice;
import org.openhab.binding.huesync.internal.connection.HueSyncDeviceConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task to handle device information update.
 * 
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class HueSyncUpdateTask implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(HueSyncUpdateTask.class);

    private final HueSyncDeviceConnection connection;
    private final HueSyncDevice deviceInfo;

    private final Consumer<@Nullable HueSyncUpdateTaskResult> action;

    public HueSyncUpdateTask(HueSyncDeviceConnection connection, HueSyncDevice deviceInfo,
            Consumer<@Nullable HueSyncUpdateTaskResult> action) {
        this.connection = connection;
        this.deviceInfo = deviceInfo;

        this.action = action;
    }

    @Override
    public void run() {
        try {
            this.logger.debug("Status update query for {} {}:{}", this.deviceInfo.name, this.deviceInfo.deviceType,
                    this.deviceInfo.uniqueId);

            if (!this.connection.isRegistered()) {
                this.action.accept(null);
            }

            HueSyncUpdateTaskResult updateInfo = new HueSyncUpdateTaskResult();

            updateInfo.deviceStatus = this.connection.getDetailedDeviceInfo();
            updateInfo.hdmiStatus = this.connection.getHdmiInfo();
            updateInfo.execution = this.connection.getExecutionInfo();

            this.action.accept(updateInfo);
        } catch (Exception e) {
            this.logger.debug("{}", e.getMessage());
            this.action.accept(null);
        }
    }
}
