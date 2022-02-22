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
package org.openhab.binding.shelly.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api.ShellyHttpApi;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.State;

/**
 * The {@link ShellyManagerInterface} implements the interface for Shelly Manager to access the thing handler
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public interface ShellyManagerInterface {

    public Thing getThing();

    public String getThingName();

    public ShellyDeviceProfile getProfile();

    public ShellyDeviceProfile getProfile(boolean forceRefresh) throws ShellyApiException;

    public ShellyHttpApi getApi();

    public ShellyDeviceStats getStats();

    public void resetStats();

    public State getChannelValue(String group, String channel);

    public void setThingOnline();

    public void setThingOffline(ThingStatusDetail detail, String messageKey);

    public boolean requestUpdates(int requestCount, boolean refreshSettings);
}
