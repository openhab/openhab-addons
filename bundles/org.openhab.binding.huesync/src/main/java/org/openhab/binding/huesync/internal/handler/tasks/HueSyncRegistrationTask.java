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
package org.openhab.binding.huesync.internal.handler.tasks;

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDevice;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistration;
import org.openhab.binding.huesync.internal.config.HueSyncConfiguration;
import org.openhab.binding.huesync.internal.connection.HueSyncDeviceConnection;
import org.openhab.binding.huesync.internal.exceptions.HueSyncTaskException;
import org.openhab.binding.huesync.internal.types.HueSyncExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task to handle device registration.
 * 
 * @author Patrik Gfeller - Initial contribution
 * @author Patrik Gfeller - Issue #18376, Fix/improve log message and exception handling
 */
@NonNullByDefault
public class HueSyncRegistrationTask implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(HueSyncRegistrationTask.class);

    private final HueSyncDeviceConnection connection;
    private final HueSyncDevice deviceInfo;
    private final HueSyncExceptionHandler exceptionHandler;
    private final Consumer<HueSyncRegistration> registrationAccepted;
    private final HueSyncConfiguration configuration;

    public HueSyncRegistrationTask(HueSyncDeviceConnection connection, HueSyncDevice deviceInfo,
            HueSyncConfiguration configuration, Consumer<HueSyncRegistration> registrationAccepted,
            HueSyncExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        this.connection = connection;
        this.deviceInfo = deviceInfo;
        this.registrationAccepted = registrationAccepted;
        this.configuration = configuration;
    }

    @Override
    public void run() {
        try {
            String id = this.deviceInfo.uniqueId;
            HueSyncRegistration registration;

            if (this.connection.isRegistered()) {
                this.logger.debug("API token for {} already configured", this.deviceInfo.name);

                registration = new HueSyncRegistration();
                registration.registrationId = this.configuration.registrationId;
                registration.accessToken = this.configuration.apiAccessToken;

                this.registrationAccepted.accept(registration);
                return;
            }

            this.logger.debug("Listening for device registration - {} {}:{}", this.deviceInfo.name,
                    this.deviceInfo.deviceType, id);

            if (id == null) {
                throw new HueSyncTaskException("Device information id must not be null");
            }

            registration = this.connection.registerDevice(id);

            if (registration != null) {
                this.logger.debug("API token for {} received", this.deviceInfo.name);
                this.registrationAccepted.accept(registration);
            }
        } catch (Exception e) {
            this.exceptionHandler.handle(e);
        }
    }
}
