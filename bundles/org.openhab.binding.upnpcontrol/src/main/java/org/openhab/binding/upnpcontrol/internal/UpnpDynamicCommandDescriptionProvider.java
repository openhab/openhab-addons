/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.upnpcontrol.internal;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.DynamicCommandDescriptionProvider;
import org.openhab.core.types.CommandDescription;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mark Herwege - Initial contribution
 */
@Component(service = { DynamicCommandDescriptionProvider.class, UpnpDynamicCommandDescriptionProvider.class })
@NonNullByDefault
public class UpnpDynamicCommandDescriptionProvider implements DynamicCommandDescriptionProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<ChannelUID, @Nullable CommandDescription> descriptions = new ConcurrentHashMap<>();

    public void setDescription(ChannelUID channelUID, @Nullable CommandDescription description) {
        logger.debug("Adding command description for channel {}", channelUID);
        descriptions.put(channelUID, description);
    }

    public void removeAllDescriptions() {
        logger.debug("Removing all command descriptions");
        descriptions.clear();
    }

    @Override
    public @Nullable CommandDescription getCommandDescription(Channel channel,
            @Nullable CommandDescription originalCommandDescription, @Nullable Locale locale) {
        return descriptions.get(channel.getUID());
    }

    @Deactivate
    public void deactivate() {
        descriptions.clear();
    }
}
