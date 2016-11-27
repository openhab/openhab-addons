/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler.welcome;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.List;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.config.NetatmoWelcomePersonConfiguration;
import org.openhab.binding.netatmo.handler.NetatmoWelcomeHandler;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;

import io.swagger.client.model.NAWelcomeEvents;
import io.swagger.client.model.NAWelcomePersons;

/**
 * {@link NAWelcomePersonHandler} is the class used to handle the Welcome Home Data
 *
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */
public class NAWelcomePersonHandler extends NetatmoWelcomeHandler<NetatmoWelcomePersonConfiguration> {

    protected NAWelcomePersons person;
    private NAWelcomeEvents event;

    public NAWelcomePersonHandler(Thing thing) {
        super(thing, NetatmoWelcomePersonConfiguration.class);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    public String getParentId() {
        return configuration.getParentId();
    }

    public String getId() {
        return configuration.getId();
    }

    @Override
    protected void updateChannels() {
        try {
            for (Thing thing : getBridgeHandler().getThing().getThings()) {
                ThingHandler thingHandler = thing.getHandler();
                if (thingHandler instanceof NAWelcomeHomeHandler) {
                    NAWelcomeHomeHandler welcomeHomeHandler = (NAWelcomeHomeHandler) thingHandler;
                    String parentId = welcomeHomeHandler.getId();
                    if (parentId != null && parentId.equals(getParentId())) {

                        if (getId() != null && getId().startsWith("unknownperson")) {
                            int index = Integer.parseInt((getId().substring(getId().indexOf('#') + 1)));

                            int i = 0;
                            for (NAWelcomePersons myPerson : getWelcomeHomes(getParentId()).getPersons()) {
                                if (myPerson.getPseudo() == null && index == ++i) {
                                    this.person = myPerson;
                                    super.updateChannels();

                                    try {
                                        List<NAWelcomeEvents> myEvents = getWelcomeHomes(getParentId()).getEvents();
                                        for (NAWelcomeEvents myEvent : myEvents) {
                                            if (myEvent.getPersonId().equals(getId())) {
                                                this.event = myEvent;
                                                break;
                                            }
                                        }
                                    } catch (Exception e) {
                                        this.event = null;
                                    }

                                    break;
                                }
                            }
                        } else {
                            for (NAWelcomePersons myPerson : getWelcomeHomes(getParentId()).getPersons()) {
                                if (myPerson.getId().equalsIgnoreCase(getId())) {
                                    this.person = myPerson;
                                    super.updateChannels();

                                    try {
                                        List<NAWelcomeEvents> myEvents = getWelcomeHomes(getParentId()).getEvents();
                                        for (NAWelcomeEvents myEvent : myEvents) {
                                            if (myEvent.getPersonId().equals(getId())) {
                                                this.event = myEvent;
                                                break;
                                            }
                                        }

                                    } catch (Exception e) {
                                        this.event = null;
                                    }

                                    break;
                                }
                            }
                        }

                    }
                }
            }

        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }
    }

    @Override
    protected State getNAThingProperty(String chanelId) {
        try {
            switch (chanelId) {
                case CHANNEL_WELCOME_PERSON_ID:
                    return person.getId() != null ? new StringType(person.getId()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_LASTSEEN:
                    return person.getId() != null ? ChannelTypeUtils.toDateTimeType(person.getLastSeen())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_OUTOFSIGHT:
                    return person.getOutOfSight() != null ? (person.getOutOfSight() ? OnOffType.ON : OnOffType.OFF)
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_FACEID:
                    return person.getFace().getId() != null ? new StringType(person.getFace().getId())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_FACEVERSION:
                    return person.getFace().getVersion() != null ? new DecimalType(person.getFace().getVersion())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_FACEKEY:
                    return person.getFace().getKey() != null ? new StringType(person.getFace().getKey())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_PSEUDO:
                    return person.getPseudo() != null ? new StringType(person.getPseudo()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_ATHOME:
                    return person.getOutOfSight() != null ? (person.getOutOfSight() ? OnOffType.OFF : OnOffType.ON)
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_LASTEVENTID:
                    return event.getId() != null ? new StringType(event.getId()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_LASTMESSAGE:
                    return event.getMessage() != null ? new StringType(event.getMessage()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_LASTTIME:
                    return event.getTime() != null ? ChannelTypeUtils.toDateTimeType(event.getTime()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_AVATARPICTURE_URL:
                    return person.getFace() != null ? getPictureUrl(person.getFace().getId(), person.getFace().getKey())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_PERSON_LASTEVENTPICTURE_URL:
                    return event.getSnapshot() != null
                            ? getPictureUrl(event.getSnapshot().getId(), event.getSnapshot().getKey())
                            : UnDefType.UNDEF;

                default:
                    return super.getNAThingProperty(chanelId);
            }
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

}