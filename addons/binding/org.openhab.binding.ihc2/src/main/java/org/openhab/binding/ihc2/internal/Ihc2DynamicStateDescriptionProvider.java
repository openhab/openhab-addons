/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.internal;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.type.DynamicStateDescriptionProvider;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.ihc2.internal.ws.Ihc2Client;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Ihc2DynamicStateDescriptionProvider} is responsible for dynamically setting the
 * State Description.
 *
 * @author Niels Peter Enemark - Initial contribution
 */
@NonNullByDefault
@Component(service = { DynamicStateDescriptionProvider.class,
        Ihc2DynamicStateDescriptionProvider.class }, immediate = true)

public class Ihc2DynamicStateDescriptionProvider implements DynamicStateDescriptionProvider {

    private final Logger logger = LoggerFactory.getLogger(Ihc2DynamicStateDescriptionProvider.class);
    private final Ihc2Client ihcClient = Ihc2Client.getInstance();

    @Override
    public @Nullable StateDescription getStateDescription(@NonNull Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {
        logger.debug("StateDescription getStateDescription(): {} {}", channel.getChannelTypeUID().getId(),
                channel.getUID().getId()); // dateTime dateTimeValue

        StateDescription sd = ihcClient.getStateDescription(channel.getUID());
        return sd;
    }
}
