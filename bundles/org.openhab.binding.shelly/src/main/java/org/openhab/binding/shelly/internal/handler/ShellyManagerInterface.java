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
package org.openhab.binding.shelly.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
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

    Thing getThing();

    String getThingName();

    ShellyDeviceProfile getProfile();

    ShellyDeviceProfile getProfile(boolean forceRefresh) throws ShellyApiException;

    ShellyApiInterface getApi();

    ShellyDeviceStats getStats();

    void resetStats();

    State getChannelValue(String group, String channel);

    void setThingOnline();

    void setThingOffline(ThingStatusDetail detail, String messageKey, Object... arguments);

    boolean requestUpdates(int requestCount, boolean refreshSettings);

    void incProtMessages();

    void incProtErrors();
}
