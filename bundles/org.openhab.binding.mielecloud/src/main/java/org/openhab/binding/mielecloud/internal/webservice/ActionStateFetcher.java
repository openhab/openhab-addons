/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.webservice.api.DeviceState;
import org.openhab.binding.mielecloud.internal.webservice.exception.AuthorizationFailedException;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceException;
import org.openhab.binding.mielecloud.internal.webservice.exception.TooManyRequestsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ActionStateFetcher} fetches the updated actions state for a device from the {@link MieleWebservice} if
 * the state of that device changed.
 *
 * Note that an instance of this class is required for each device.
 *
 * @author Roland Edelhoff - Initial contribution
 * @author Björn Lange - Make calls to webservice asynchronous
 */
@NonNullByDefault
public class ActionStateFetcher {
    private Optional<DeviceState> lastDeviceState = Optional.empty();
    private final Supplier<MieleWebservice> webserviceSupplier;
    private final ScheduledExecutorService scheduler;

    private final Logger logger = LoggerFactory.getLogger(ActionStateFetcher.class);

    /**
     * Creates a new {@link ActionStateFetcher}.
     *
     * @param webserviceSupplier Getter function for access to the {@link MieleWebservice}.
     * @param scheduler System-wide scheduler.
     */
    public ActionStateFetcher(Supplier<MieleWebservice> webserviceSupplier, ScheduledExecutorService scheduler) {
        this.webserviceSupplier = webserviceSupplier;
        this.scheduler = scheduler;
    }

    /**
     * Invoked when the state of a device was updated.
     */
    public void onDeviceStateUpdated(DeviceState deviceState) {
        if (hasDeviceStatusChanged(deviceState)) {
            scheduler.submit(() -> fetchActions(deviceState));
        }
        lastDeviceState = Optional.of(deviceState);
    }

    private boolean hasDeviceStatusChanged(DeviceState newDeviceState) {
        return lastDeviceState.map(DeviceState::getStateType)
                .map(rawStatus -> !newDeviceState.getStateType().equals(rawStatus)).orElse(true);
    }

    private void fetchActions(DeviceState deviceState) {
        try {
            webserviceSupplier.get().fetchActions(deviceState.getDeviceIdentifier());
        } catch (MieleWebserviceException e) {
            logger.warn("Failed to fetch action state for device {}: {} - {}", deviceState.getDeviceIdentifier(),
                    e.getConnectionError(), e.getMessage());
        } catch (AuthorizationFailedException | TooManyRequestsException e) {
            logger.warn("Failed to fetch action state for device {}: {}", deviceState.getDeviceIdentifier(),
                    e.getMessage());
        }
    }
}
