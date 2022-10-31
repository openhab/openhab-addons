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
package org.openhab.binding.boschshc.internal.devices;

import org.openhab.core.config.core.Configuration;

/**
 * Abstract unit test implementation for device handlers.
 *
 * @author David Pace - Initial contribution
 *
 * @param <T> type of the device handler to be tested
 */
public abstract class AbstractBoschSHCDeviceHandlerTest<T extends BoschSHCDeviceHandler>
        extends AbstractSHCHandlerTest<T> {

    @Override
    protected Configuration getConfiguration() {
        Configuration configuration = super.getConfiguration();
        configuration.put("id", getDeviceID());
        return configuration;
    }

    protected abstract String getDeviceID();
}
