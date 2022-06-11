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
package org.openhab.binding.netatmo.internal.handler.channelhelper;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.toRawType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.HomeData;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusPerson;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus.HomeStatus;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link SecurityChannelHelper} handles specific information for security purpose.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class SecurityChannelHelper extends ChannelHelper {
    private long persons = -1;
    private long unknowns = -1;
    private @Nullable String unknownSnapshot;
    private List<String> knownIds = List.of();

    public SecurityChannelHelper(Set<String> providedGroups) {
        super(providedGroups);
    }

    @Override
    public void setNewData(@Nullable NAObject data) {
        super.setNewData(data);
        if (data instanceof HomeData) {
            HomeData homeData = (HomeData) data;
            knownIds = homeData.getPersons().values().stream().filter(person -> person.isKnown()).map(p -> p.getId())
                    .collect(Collectors.toList());
        }
        if (data instanceof HomeStatus) {
            HomeStatus status = (HomeStatus) data;
            NAObjectMap<HomeStatusPerson> allPersons = status.getPersons();
            List<HomeStatusPerson> present = allPersons.values().stream().filter(p -> !p.isOutOfSight())
                    .collect(Collectors.toList());

            persons = present.size();
            unknowns = present.stream().filter(person -> !knownIds.contains(person.getId())).count();
        }
    }

    @Override
    protected @Nullable State internalGetOther(String channelId) {
        switch (channelId) {
            case CHANNEL_PERSON_COUNT:
                return persons != -1 ? new DecimalType(persons) : UnDefType.NULL;
            case CHANNEL_UNKNOWN_PERSON_COUNT:
                return unknowns != -1 ? new DecimalType(unknowns) : UnDefType.NULL;
            case CHANNEL_UNKNOWN_PERSON_PICTURE:
                return unknownSnapshot != null ? toRawType(unknownSnapshot) : UnDefType.NULL;
        }
        return null;
    }
}
