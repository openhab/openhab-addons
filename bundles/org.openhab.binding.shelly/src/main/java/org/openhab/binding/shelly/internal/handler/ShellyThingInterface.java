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

    ShellyDeviceProfile getProfile(boolean forceRefresh) throws ShellyApiException;

    @Nullable
    List<StateOption> getStateOptions(ChannelTypeUID uid);

    double getChannelDouble(String group, String channel);

    boolean updateChannel(String group, String channel, State value);

    boolean updateChannel(String channelId, State value, boolean force);

    void setThingOnline();

    void setThingOffline(ThingStatusDetail detail, String messageKey, Object... arguments);

    boolean isStopping();

    String getThingType();

    ThingStatus getThingStatus();

    ThingStatusDetail getThingStatusDetail();

    boolean isThingOnline();

    boolean requestUpdates(int requestCount, boolean refreshSettings);

    void triggerUpdateFromCoap();

    void reinitializeThing();

    void restartWatchdog();

    void publishState(String channelId, State value);

    boolean areChannelsCreated();

    State getChannelValue(String group, String channel);

    boolean updateInputs(ShellySettingsStatus status);

    void updateChannelDefinitions(Map<String, Channel> dynChannels);

    void postEvent(String event, boolean force);

    void triggerChannel(String group, String channelID, String event);

    void triggerButton(String group, int idx, String value);

    ShellyDeviceStats getStats();

    void resetStats();

    Thing getThing();

    String getThingName();

    ShellyThingConfiguration getThingConfig();

    HttpClient getHttpClient();

    String getProperty(String key);

    void updateProperties(String key, String value);

    boolean updateWakeupReason(@Nullable List<Object> valueArray);

    ShellyApiInterface getApi();

    ShellyDeviceProfile getProfile();

    long getScheduledUpdates();

    void fillDeviceStatus(ShellySettingsStatus status, boolean updated);

    boolean checkRepresentation(String key);

    void incProtMessages();

    void incProtErrors();
}
