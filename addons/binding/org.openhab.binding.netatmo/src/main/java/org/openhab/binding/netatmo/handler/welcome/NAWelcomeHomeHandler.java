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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.config.NetatmoWelcomeConfiguration;
import org.openhab.binding.netatmo.handler.NetatmoDeviceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.client.model.NAWelcomeEvents;
import io.swagger.client.model.NAWelcomeHomeData;
import io.swagger.client.model.NAWelcomeHomes;
import io.swagger.client.model.NAWelcomePersons;

/**
 * {@link NAWelcomeHomeHandler} is the class used to handle the Welcome Home Data
 *
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */

public class NAWelcomeHomeHandler extends AbstractNetatmoWelcomeHandler {
    private static Logger logger = LoggerFactory.getLogger(NetatmoDeviceHandler.class);

    private NetatmoWelcomeConfiguration configuration;
    private ScheduledFuture<?> refreshJob;

    private int iPerson = -1;
    private int iUnknown = -1;
    private boolean bSomebodyAtHome = false;

    public NAWelcomeHomeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
    }

    @Override
    public void initialize() {
        super.initialize();

        this.configuration = this.getConfigAs(NetatmoWelcomeConfiguration.class);

        refreshJob = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                updateChannels();
            }
        }, 60000, configuration.refreshInterval, TimeUnit.MILLISECONDS);

    }

    @Override
    public void dispose() {
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    public String getId() {
        return configuration.getWelcomeHomeId();
    }

    @Override
    protected void updateChannels() {
        try {
            updateStatus(ThingStatus.INITIALIZING);

            NAWelcomeHomeData myHomeDate = bridgeHandler.getWelcomeApi().gethomedata(getId(), null).getBody();
            for (NAWelcomeHomes myHome : myHomeDate.getHomes()) {
                if (myHome.getId().equalsIgnoreCase(getId())) {

                    Collections.sort(myHome.getEvents(), new Comparator<NAWelcomeEvents>() {
                        @Override
                        public int compare(NAWelcomeEvents s1, NAWelcomeEvents s2) {
                            return s2.getTime().compareTo(s1.getTime());
                        }
                    });

                    Collections.sort(myHome.getPersons(), new Comparator<NAWelcomePersons>() {
                        @Override
                        public int compare(NAWelcomePersons s1, NAWelcomePersons s2) {
                            return s2.getLastSeen().compareTo(s1.getLastSeen());
                        }
                    });

                    setWelcomeHomes(myHome.getId(), myHome);

                    // Check if somebody is at home
                    iPerson = 0;
                    iUnknown = 0;
                    bSomebodyAtHome = false;
                    HashMap<String, NAWelcomePersons> foundPerson = new HashMap<String, NAWelcomePersons>();
                    myHome.getPersons();
                    for (NAWelcomePersons person : myHome.getPersons()) {
                        if (foundPerson.get(person.getId()) == null) {
                            foundPerson.put(person.getId(), person);
                            if (person.getPseudo() != null) {
                                if (!person.getOutOfSight()) {
                                    iPerson++;
                                }
                            } else {
                                if (!person.getOutOfSight()) {
                                    iUnknown++;
                                }
                            }
                        }
                    }
                    if (iPerson > 0 || iUnknown > 0) {
                        bSomebodyAtHome = true;
                    }

                    super.updateChannels();
                    updateWelcomeThings();
                    break;
                }
            }

            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);

        } catch (Throwable e) {
            logger.error("Exception when trying to update channels: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }
    }

    @Override
    protected State getNAThingProperty(String chanelId) {
        try {
            switch (chanelId) {
                case CHANNEL_WELCOME_HOME_ID:
                    return getWelcomeHomes(getId()).getId() != null ? new StringType(getWelcomeHomes(getId()).getId())
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_HOME_NAME:
                    return getWelcomeHomes(getId()).getName() != null
                            ? new StringType(getWelcomeHomes(getId()).getName()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_HOME_CITY:
                    return getWelcomeHomes(getId()).getPlace().getCity() != null
                            ? new StringType(getWelcomeHomes(getId()).getPlace().getCity()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_HOME_COUNTRY:
                    return getWelcomeHomes(getId()).getPlace().getCountry() != null
                            ? new StringType(getWelcomeHomes(getId()).getPlace().getCountry()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_HOME_TIMEZONE:
                    return getWelcomeHomes(getId()).getPlace().getTimezone() != null
                            ? new StringType(getWelcomeHomes(getId()).getPlace().getTimezone()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_HOME_SOMEBODYATHOME:
                    return bSomebodyAtHome ? OnOffType.ON : OnOffType.OFF;
                case CHANNEL_WELCOME_HOME_PERSONCOUNT:
                    return iPerson != -1 ? new DecimalType(iPerson) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_HOME_UNKNOWNCOUNT:
                    return iUnknown != -1 ? new DecimalType(iUnknown) : UnDefType.UNDEF;

                default:
                    return super.getNAThingProperty(chanelId);
            }
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    private void updateWelcomeThings() {
        for (Thing handler : bridgeHandler.getThing().getThings()) {
            ThingHandler thingHandler = handler.getHandler();
            if (thingHandler instanceof NAWelcomeCameraHandler) {
                NAWelcomeCameraHandler welcomeHandler = (NAWelcomeCameraHandler) thingHandler;
                String parentId = welcomeHandler.getParentId();
                if (parentId != null && parentId.equals(getId())) {
                    welcomeHandler.updateChannels();
                }
            } else if (thingHandler instanceof NAWelcomePersonHandler) {
                NAWelcomePersonHandler welcomeHandler = (NAWelcomePersonHandler) thingHandler;
                String parentId = welcomeHandler.getParentId();
                if (parentId != null && parentId.equals(getId())) {
                    welcomeHandler.updateChannels();
                }
            }
            if (thingHandler instanceof NAWelcomeEventHandler) {
                NAWelcomeEventHandler welcomeHandler = (NAWelcomeEventHandler) thingHandler;
                String parentId = welcomeHandler.getParentId();
                if (parentId != null && parentId.equals(getId())) {
                    welcomeHandler.updateChannels();
                }
            }
        }
    }
}
