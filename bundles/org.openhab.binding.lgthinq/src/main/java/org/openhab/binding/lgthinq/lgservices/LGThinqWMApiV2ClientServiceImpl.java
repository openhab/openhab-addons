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
package org.openhab.binding.lgthinq.lgservices;

import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;

/**
 * The {@link LGThinqWMApiV2ClientServiceImpl}
 *
 * @author Nemer Daud - Initial contribution
 */
public class LGThinqWMApiV2ClientServiceImpl extends LGThinqApiClientServiceImpl implements LGThinqWMApiClientService {

    private static final LGThinqWMApiClientService instance;
    static {
        instance = new LGThinqWMApiV2ClientServiceImpl();
    }

    public static LGThinqWMApiClientService getInstance() {
        return instance;
    }

    @Override
    public void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState)
            throws LGThinqApiException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
