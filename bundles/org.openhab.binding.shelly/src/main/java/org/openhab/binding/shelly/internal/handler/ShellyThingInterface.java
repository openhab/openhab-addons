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

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;

/**
 * The {@link ShellyThingInterface} implements the interface for Shelly Manager to access the thing handler
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public interface ShellyThingInterface {

    public ShellyDeviceProfile getProfile(boolean forceRefresh) throws ShellyApiException;

    public List<StateOption> getStateOptions(ChannelTypeUID uid);

    public double getChannelDouble(String group, String channel);

    public boolean updateChannel(String group, String channel, State value);

    public boolean updateChannel(String channelId, State value, boolean force);

    public void setThingOnline();

    public void setThingOffline(ThingStatusDetail detail, String messageKey);

    public ThingStatus getThingStatus();

    public ThingStatusDetail getThingStatusDetail();

    public boolean isThingOnline();

    public boolean requestUpdates(int requestCount, boolean refreshSettings);

    public void triggerUpdateFromCoap();

    public void reinitializeThing();

    public void restartWatchdog();

    public void publishState(String channelId, State value);

    public boolean areChannelsCreated();

    public State getChannelValue(String group, String channel);

    public boolean updateInputs(ShellySettingsStatus status);

    public void updateChannelDefinitions(Map<String, Channel> dynChannels);

    public void postEvent(String event, boolean force);

    public void triggerChannel(String group, String channelID, String event);

    public void triggerButton(String group, int idx, String value);

    public ShellyDeviceStats getStats();

    public void resetStats();

    public Thing getThing();

    public String getThingName();

    public ShellyThingConfiguration getThingConfig();

    public HttpClient getHttpClient();

    public String getProperty(String key);

    public void updateProperties(String key, String value);

    public boolean updateWakeupReason(@Nullable List<Object> valueArray);

    public ShellyApiInterface getApi();

    public ShellyDeviceProfile getProfile();

    public long getScheduledUpdates();

    public void fillDeviceStatus(ShellySettingsStatus status, boolean updated);

    public boolean checkRepresentation(String key);
}
