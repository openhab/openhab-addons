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
package org.openhab.binding.netatmo.internal.handler.channelhelper;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.HomeDataPerson;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusPerson;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 * The {@link PersonChannelHelper} handles channels of person things.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class PersonChannelHelper extends ChannelHelper {

    public PersonChannelHelper(Set<String> providedGroups) {
        super(providedGroups);
    }

    @Override
    protected @Nullable State internalGetProperty(String channelId, NAThing naThing, Configuration config) {
        if (naThing instanceof HomeDataPerson person) {
            switch (channelId) {
                case CHANNEL_PERSON_AVATAR_URL:
                    return toStringType(person.getUrl().orElse(null));
                case CHANNEL_PERSON_AVATAR:
                    return toRawType(person.getUrl().orElse(null));
            }
        }
        if (naThing instanceof HomeStatusPerson person) {
            switch (channelId) {
                case CHANNEL_PERSON_AT_HOME:
                    return OnOffType.from(person.atHome());
                case CHANNEL_LAST_SEEN:
                    return toDateTimeType(person.getLastSeen());
            }
        }
        return null;
    }
}
