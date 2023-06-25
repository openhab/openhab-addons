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
package org.openhab.binding.lgthinq.lgservices;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.api.RestResult;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerSnapshot;

/**
 * The {@link LGThinQDRApiV2ClientServiceImpl}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQDRApiV2ClientServiceImpl
        extends LGThinQAbstractApiV2ClientService<WasherDryerCapability, WasherDryerSnapshot>
        implements LGThinQDRApiClientService {

    private static final LGThinQDRApiV2ClientServiceImpl instance;
    static {
        instance = new LGThinQDRApiV2ClientServiceImpl(WasherDryerCapability.class, WasherDryerSnapshot.class);
    }

    protected LGThinQDRApiV2ClientServiceImpl(Class<WasherDryerCapability> capabilityClass,
            Class<WasherDryerSnapshot> snapshotClass) {
        super(capabilityClass, snapshotClass);
    }

    @Override
    protected void beforeGetDataDevice(@NonNull String bridgeName, @NonNull String deviceId) {
        // TODO - Analise what to do here
    }

    public static LGThinQDRApiV2ClientServiceImpl getInstance() {
        return instance;
    }

    @Override
    public void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState)
            throws LGThinqApiException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void remoteStart(String bridgeName, String deviceId) throws LGThinqApiException {
        try {
            RestResult result = sendCommand(bridgeName, deviceId, "control-sync", "WMStart", "WMStart", "WMStart", "");
            handleGenericErrorResult(result);
        } catch (LGThinqApiException e) {
            throw e;
        } catch (Exception e) {
            throw new LGThinqApiException("Error sending remote start", e);
        }
    }

    @Override
    public void wakeUp(String bridgeName, String deviceId) throws LGThinqApiException {
        try {
            RestResult result = sendCommand(bridgeName, deviceId, "control-sync", "WMWakeup", "WMWakeup", "", "");
            handleGenericErrorResult(result);
        } catch (LGThinqApiException e) {
            throw e;
        } catch (Exception e) {
            throw new LGThinqApiException("Error sending remote start", e);
        }
    }
}
