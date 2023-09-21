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
package org.openhab.binding.verisure.internal.handler;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.verisure.internal.dto.VerisureUserPresencesDTO;
import org.openhab.binding.verisure.internal.dto.VerisureUserPresencesDTO.UserTracking;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Handler for the User Presence Device thing type that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureUserPresenceThingHandler extends VerisureThingHandler<VerisureUserPresencesDTO> {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_USERPRESENCE);

    public VerisureUserPresenceThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public Class<VerisureUserPresencesDTO> getVerisureThingClass() {
        return VerisureUserPresencesDTO.class;
    }

    @Override
    public synchronized void update(VerisureUserPresencesDTO thing) {
        updateUserPresenceState(thing);
        updateStatus(ThingStatus.ONLINE);
    }

    private void updateUserPresenceState(VerisureUserPresencesDTO userPresenceJSON) {
        List<UserTracking> userTrackingList = userPresenceJSON.getData().getInstallation().getUserTrackings();
        if (!userTrackingList.isEmpty()) {
            UserTracking userTracking = userTrackingList.get(0);
            getThing().getChannels().stream().map(Channel::getUID)
                    .filter(channelUID -> isLinked(channelUID) && !"timestamp".equals(channelUID.getId()))
                    .forEach(channelUID -> {
                        State state = getValue(channelUID.getId(), userTracking);
                        updateState(channelUID, state);
                    });
            updateTimeStamp(userTracking.getCurrentLocationTimestamp());
            updateInstallationChannels(userPresenceJSON);
        } else {
            logger.debug("UserTrackingList is empty!");
        }
    }

    public State getValue(String channelId, UserTracking userTracking) {
        switch (channelId) {
            case CHANNEL_USER_NAME:
                String name = userTracking.getName();
                return name != null ? new StringType(name) : UnDefType.NULL;
            case CHANNEL_USER_LOCATION_STATUS:
                String currentLocation = userTracking.getCurrentLocationName();
                return currentLocation != null ? new StringType(currentLocation)
                        : new StringType(userTracking.getCurrentLocationId());
            case CHANNEL_STATUS:
                String status = userTracking.getStatus();
                return status != null ? new StringType(status) : UnDefType.NULL;
            case CHANNEL_WEBACCOUNT:
                String webAccount = userTracking.getWebAccount();
                return webAccount != null ? new StringType(webAccount) : UnDefType.NULL;
            case CHANNEL_USER_DEVICE_NAME:
                String deviceName = userTracking.getDeviceName();
                return deviceName != null ? new StringType(deviceName) : UnDefType.NULL;

        }
        return UnDefType.UNDEF;
    }

    @Override
    public void updateTriggerChannel(String event) {
    }
}
