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
package org.openhab.binding.homeconnect.internal.type;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.type.DynamicStateDescriptionProvider;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.homeconnect.internal.logger.EmbeddedLoggingService;
import org.openhab.binding.homeconnect.internal.logger.LogWriter;
import org.openhab.binding.homeconnect.internal.logger.Type;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.event.Level;

/**
 * The {@link HomeConnectDynamicStateDescriptionProvider} is responsible for handling dynamic thing values.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, HomeConnectDynamicStateDescriptionProvider.class })
@NonNullByDefault
public class HomeConnectDynamicStateDescriptionProvider implements DynamicStateDescriptionProvider {

    private final ConcurrentHashMap<String, StateDescription> stateDescriptions = new ConcurrentHashMap<>();
    private final LogWriter logger;

    @Activate
    public HomeConnectDynamicStateDescriptionProvider(@Reference EmbeddedLoggingService loggingService) {
        logger = loggingService.getLogger(HomeConnectDynamicStateDescriptionProvider.class);
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {
        if (stateDescriptions.containsKey(channel.getUID().getAsString())) {
            logger.log(Type.DEFAULT, Level.DEBUG, null, null,
                    Arrays.asList(stateDescriptions.get(channel.getUID().getAsString()).toString()), null, null,
                    "Return dynamic state description for channel-uid {}.", channel.getUID().getAsString());
            return stateDescriptions.get(channel.getUID().getAsString());
        }

        return originalStateDescription;
    }

    public void putStateDescriptions(String channelUid, StateDescription stateDescription) {
        logger.log(Type.DEFAULT, Level.DEBUG, null, null, Arrays.asList(stateDescription.toString()), null, null,
                "Adding state description for channel-uid: {}", channelUid);
        stateDescriptions.put(channelUid, stateDescription);
    }

    public void removeStateDescriptions(String channelUid) {
        logger.debug("Removing state description for channel-uid: {}.", channelUid);
        stateDescriptions.remove(channelUid);
    }

}
