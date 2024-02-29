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
package org.openhab.binding.boschshc.internal.devices;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Device;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * Abstract unit test implementation for device handlers.
 *
 * @author David Pace - Initial contribution
 *
 * @param <T> type of the device handler to be tested
 */
@NonNullByDefault
public abstract class AbstractBoschSHCDeviceHandlerTest<T extends BoschSHCDeviceHandler>
        extends AbstractBoschSHCHandlerTest<T> {

    @Override
    protected void configureDevice(Device device) {
        super.configureDevice(device);

        device.id = getDeviceID();
    }

    @Override
    protected Configuration getConfiguration() {
        Configuration configuration = super.getConfiguration();
        configuration.put("id", getDeviceID());
        return configuration;
    }

    protected abstract String getDeviceID();

    @Test
    void initializeInvalidDeviceId() {
        getFixture().getThing().getConfiguration().remove("id");
        getFixture().initialize();

        verify(getCallback()).statusUpdated(eq(getThing()),
                argThat(status -> status.getStatus().equals(ThingStatus.OFFLINE)
                        && status.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
    }

    @ParameterizedTest
    @MethodSource("org.openhab.binding.boschshc.internal.tests.common.CommonTestUtils#getExecutionExceptionAndInterruptedExceptionArguments()")
    void initializeHandleExceptionDuringDeviceInfoRestCall(Exception exception)
            throws BoschSHCException, InterruptedException, TimeoutException, ExecutionException {
        when(getBridgeHandler().getDeviceInfo(getDeviceID())).thenThrow(exception);

        getFixture().initialize();

        verify(getCallback()).statusUpdated(eq(getThing()),
                argThat(status -> status.getStatus().equals(ThingStatus.OFFLINE)
                        && status.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
    }
}
