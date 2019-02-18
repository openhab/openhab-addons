/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.zoneminder.internal.config;

import org.openhab.binding.zoneminder.internal.ZoneMinderConstants;

/**
 * Monitor configuration from openHAB.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public abstract class ZoneMinderThingConfig extends ZoneMinderConfig {

    public abstract String getZoneMinderId();

    @Override
    public String getConfigId() {
        return ZoneMinderConstants.THING_ZONEMINDER_MONITOR;
    }

}
