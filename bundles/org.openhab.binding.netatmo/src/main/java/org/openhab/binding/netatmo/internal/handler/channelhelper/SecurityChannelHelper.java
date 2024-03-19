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
package org.openhab.binding.netatmo.internal.handler.channelhelper;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.toRawType;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.dto.HomeData;
import org.openhab.binding.netatmo.internal.api.dto.HomeDataPerson;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusPerson;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus.HomeStatus;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
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
        if (data instanceof HomeData.Security securityData) {
            knownIds = securityData.getKnownPersons().stream().map(HomeDataPerson::getId).toList();
        } else if (data instanceof HomeStatus status && status.appliesTo(FeatureArea.SECURITY)) {
            List<String> present = status.getPersons().values().stream().filter(HomeStatusPerson::atHome)
                    .map(HomeStatusPerson::getId).toList();

            persons = present.size();
            unknowns = present.stream().filter(Predicate.not(knownIds::contains)).count();
        }
    }

    @Override
    protected @Nullable State internalGetOther(String channelId) {
        return switch (channelId) {
            case CHANNEL_PERSON_COUNT -> persons != -1 ? new DecimalType(persons) : UnDefType.NULL;
            case CHANNEL_UNKNOWN_PERSON_COUNT -> unknowns != -1 ? new DecimalType(unknowns) : UnDefType.NULL;
            case CHANNEL_UNKNOWN_PERSON_PICTURE ->
                unknownSnapshot != null ? toRawType(unknownSnapshot) : UnDefType.NULL;
            default -> null;
        };
    }
}
