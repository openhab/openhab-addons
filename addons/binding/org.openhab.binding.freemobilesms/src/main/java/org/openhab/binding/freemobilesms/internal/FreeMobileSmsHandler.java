/**
 * Copyright (c) 2014,2019 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.freemobilesms.internal;

import static org.openhab.binding.freemobilesms.internal.FreeMobileSmsBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freemobilesms.internal.FreeMobileSmsConfiguration;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.free.smsapi.Account;
import fr.free.smsapi.RawAccount;

/**
 * The {@link FreeMobileSmsHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Guilhem Bonnefille <guilhem.bonnefille@gmail.com> - Initial contribution
 */
@NonNullByDefault
public class FreeMobileSmsHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FreeMobileSmsHandler.class);

    private FreeMobileSmsConfiguration config;

    private Account account;

    public FreeMobileSmsHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_MESSAGE.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            if (command instanceof StringType) {
                // TODO: handle data set
            }

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        // logger.debug("Start initializing!");
        config = getConfigAs(FreeMobileSmsConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            // Check configuration
            if (config != null && config.user != null && config.password != null) {
              account = new RawAccount(config.user, config.password);
              updateStatus(ThingStatus.ONLINE);
            } else {
              updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                      "Failed to retrieve configuration");
            }
            // TODO check network
        });

        // logger.debug("Finished initializing!");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
