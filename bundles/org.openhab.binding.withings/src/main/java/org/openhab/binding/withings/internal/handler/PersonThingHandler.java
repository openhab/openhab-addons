/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.withings.internal.handler;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.withings.internal.WithingsBindingConstants;
import org.openhab.binding.withings.internal.api.WithingsDataModel;
import org.openhab.binding.withings.internal.exception.UnknownChannelException;
import org.openhab.binding.withings.internal.service.person.Person;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.*;
import org.openhab.core.types.State;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class PersonThingHandler extends AbstractWithingsThingHandler {

    public PersonThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public boolean updateThingData(WithingsDataModel model) {
        Optional<Person> person = model.getPerson();
        if (person.isPresent()) {
            updateChannels(person.get());
            return true;
        }
        return false;
    }

    private void updateChannels(Person person) {
        for (Channel channel : getThing().getChannels()) {

            ChannelUID channelUID = channel.getUID();
            if (isLinked(channelUID)) {

                @Nullable
                State state = null;
                switch (channelUID.getId()) {
                    case WithingsBindingConstants.CHANNEL_PERSON_WEIGHT:
                        @Nullable
                        BigDecimal weight = person.getWeight();
                        if (weight != null) {
                            state = new DecimalType(weight);
                        }
                        break;
                    case WithingsBindingConstants.CHANNEL_PERSON_HEIGHT:
                        @Nullable
                        BigDecimal height = person.getHeight();
                        if (height != null) {
                            state = new DecimalType(height);
                        }
                        break;
                    case WithingsBindingConstants.CHANNEL_PERSON_FAT_MASS:
                        @Nullable
                        BigDecimal fatMass = person.getFatMass();
                        if (fatMass != null) {
                            state = new DecimalType(fatMass);
                        }
                        break;
                    case WithingsBindingConstants.CHANNEL_PERSON_LAST_SLEEP_START:
                        @Nullable
                        Date lastSleepStart = person.getLastSleepStart();
                        if (lastSleepStart != null) {
                            state = createDateTimeType(lastSleepStart);
                        }
                        break;
                    case WithingsBindingConstants.CHANNEL_PERSON_LAST_SLEEP_END:
                        @Nullable
                        Date lastSleepEnd = person.getLastSleepEnd();
                        if (lastSleepEnd != null) {
                            state = createDateTimeType(lastSleepEnd);
                        }
                        break;
                    case WithingsBindingConstants.CHANNEL_PERSON_LAST_SLEEP_SCORE:
                        @Nullable
                        Integer lastSleepScore = person.getLastSleepScore();
                        if (lastSleepScore != null) {
                            state = new DecimalType(lastSleepScore);
                        }
                        break;
                    default:
                        throw new UnknownChannelException(channelUID);
                }
                if (state != null) {
                    updateState(channelUID, state);
                }
            }
        }
    }
}
