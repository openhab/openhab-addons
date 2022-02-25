/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.sunsa.internal.bridge;

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sunsa.internal.client.SunsaService;
import org.openhab.binding.sunsa.internal.domain.Device;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;

/**
 * Bridge to communicate with the devices.
 *
 * @author jirom - Initial contribution
 */
@NonNullByDefault
public abstract class SunsaBridgeHandler extends BaseBridgeHandler implements SunsaService {
    public SunsaBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    /**
     * Returns the service (i.e. contains the main business logic) that backs this bridge.
     */
    protected abstract SunsaService getSunsaService();

    @Override
    public List<Device> getDevices() {
        return callAndSetStatus(() -> getSunsaService().getDevices());
    }

    @Override
    public Device updateDevice(Device device) {
        return callAndSetStatus(() -> getSunsaService().updateDevice(device));
    }

    @Override
    public Device getDevice(String id) throws SunsaException {
        return callAndSetStatus(() -> getSunsaService().getDevice(id));
    }

    @Override
    public int setDevicePosition(String deviceId, int rawPosition) {
        return callAndSetStatus(() -> getSunsaService().setDevicePosition(deviceId, rawPosition));
    }

    private <T> T callAndSetStatus(Supplier<T> apiCall) {
        try {
            final T result = apiCall.get();
            updateStatus(ThingStatus.ONLINE);
            return result;
        } catch (ClientException e) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            throw e;
        } catch (ServiceException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            throw e;
        } catch (SunsaException e) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            throw e;
        }
    }
}
