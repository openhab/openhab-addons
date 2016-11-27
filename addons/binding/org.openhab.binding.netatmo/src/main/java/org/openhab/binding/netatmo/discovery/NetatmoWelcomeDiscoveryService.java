/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.discovery;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.netatmo.handler.NetatmoBridgeHandler;

import io.swagger.client.model.NAWelcomeCameras;
import io.swagger.client.model.NAWelcomeEvents;
import io.swagger.client.model.NAWelcomeHomeData;
import io.swagger.client.model.NAWelcomeHomes;
import io.swagger.client.model.NAWelcomePersons;

/**
 * The {@link NetatmoWelcomeDiscoveryService} searches for available Netatmo
 * welcome cameras
 *
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */
public class NetatmoWelcomeDiscoveryService extends AbstractDiscoveryService {
    private final static int SEARCH_TIME = 2;
    private NetatmoBridgeHandler netatmoBridgeHandler;
    private int unknownCount = 0;
    private int eventCount = 0;

    public NetatmoWelcomeDiscoveryService(NetatmoBridgeHandler netatmoBridgeHandler) {
        super(org.openhab.binding.netatmo.NetatmoBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS, SEARCH_TIME);
        this.netatmoBridgeHandler = netatmoBridgeHandler;
    }

    private void screenWelcomeHomes(NAWelcomeHomeData welcomeHomeDate) {

        if (netatmoBridgeHandler != null) {
            unknownCount = netatmoBridgeHandler.getWelcomeUnknownPersonThings();
            eventCount = netatmoBridgeHandler.getWelcomeEventThings();
        }

        if (welcomeHomeDate != null) {
            List<NAWelcomeHomes> myHomes = welcomeHomeDate.getHomes();
            if (myHomes != null) {
                for (NAWelcomeHomes myHome : myHomes) {

                    onWelcomeHomeAddedInternal(myHome);

                    List<NAWelcomeCameras> myCameras = myHome.getCameras();
                    for (NAWelcomeCameras myCamera : myCameras) {
                        onWelcomeCamereAddedInternal(myHome, myCamera);
                    }

                    List<NAWelcomePersons> myPersons = myHome.getPersons();
                    Collections.sort(myPersons, new Comparator<NAWelcomePersons>() {
                        @Override
                        public int compare(NAWelcomePersons s1, NAWelcomePersons s2) {
                            return s2.getLastSeen().compareTo(s1.getLastSeen());
                        }
                    });
                    int iPerson = 1;
                    for (NAWelcomePersons myPerson : myPersons) {
                        if (myPerson.getPseudo() != null) {
                            onWelcomePersonAddedInternal(myHome, myPerson);
                        } else if (iPerson <= unknownCount) {
                            onWelcomePersonAddedInternal(myHome, myPerson, iPerson++);
                        }
                    }

                    List<NAWelcomeEvents> myEvents = myHome.getEvents();
                    Collections.sort(myEvents, new Comparator<NAWelcomeEvents>() {
                        @Override
                        public int compare(NAWelcomeEvents s1, NAWelcomeEvents s2) {
                            return s2.getTime().compareTo(s1.getTime());
                        }
                    });
                    int iEvent = 1;
                    for (NAWelcomeEvents myEvent : myEvents) {
                        if (iEvent <= eventCount) {
                            onWelcomeEventAddedInternal(myHome, myEvent, iEvent++);
                        }
                    }

                }
            }
        }

    }

    @Override
    public void startScan() {

        NAWelcomeHomeData welcomeHomeDate = netatmoBridgeHandler.getWelcomeDataBody(null);
        if (welcomeHomeDate != null) {
            screenWelcomeHomes(welcomeHomeDate);
        }

        stopScan();
    }

    private void onWelcomeHomeAddedInternal(NAWelcomeHomes naWelcomeHome) {
        ThingUID thingUID = findThingUID("NAWelcomeHome", naWelcomeHome.getId());
        Map<String, Object> properties = new HashMap<>(1);

        properties.put(org.openhab.binding.netatmo.NetatmoBindingConstants.WELCOME_HOME_ID, naWelcomeHome.getId());

        String name = naWelcomeHome.getName();

        addDiscoveredThing(thingUID, properties, name);
    }

    private void onWelcomeCamereAddedInternal(NAWelcomeHomes naWelcomeHome, NAWelcomeCameras naWelcomeCamera) {
        ThingUID thingUID = findThingUID("NAWelcomeCamera", naWelcomeCamera.getId());
        Map<String, Object> properties = new HashMap<>(1);

        properties.put(org.openhab.binding.netatmo.NetatmoBindingConstants.WELCOME_HOME_ID, naWelcomeHome.getId());
        properties.put(org.openhab.binding.netatmo.NetatmoBindingConstants.WELCOME_CAMERA_ID, naWelcomeCamera.getId());

        String name = naWelcomeCamera.getName();

        addDiscoveredThing(thingUID, properties, name);
    }

    private void onWelcomePersonAddedInternal(NAWelcomeHomes naWelcomeHome, NAWelcomePersons myPerson) {
        ThingUID thingUID = findThingUID("NAWelcomePerson", myPerson.getId());
        Map<String, Object> properties = new HashMap<>(1);

        properties.put(org.openhab.binding.netatmo.NetatmoBindingConstants.WELCOME_HOME_ID, naWelcomeHome.getId());
        properties.put(org.openhab.binding.netatmo.NetatmoBindingConstants.WELCOME_PERSON_ID, myPerson.getId());

        String name = myPerson.getPseudo();
        addDiscoveredThing(thingUID, properties, name);
    }

    private void onWelcomePersonAddedInternal(NAWelcomeHomes naWelcomeHome, NAWelcomePersons myPerson, int Index) {
        ThingUID thingUID = findThingUID("NAWelcomePerson", "UnknownPerson#" + Index);
        Map<String, Object> properties = new HashMap<>(1);

        properties.put(org.openhab.binding.netatmo.NetatmoBindingConstants.WELCOME_HOME_ID, naWelcomeHome.getId());
        properties.put(org.openhab.binding.netatmo.NetatmoBindingConstants.WELCOME_PERSON_ID, "UnknownPerson#" + Index);

        addDiscoveredThing(thingUID, properties, "Unknown Person " + Index);
    }

    private void onWelcomeEventAddedInternal(NAWelcomeHomes naWelcomeHome, NAWelcomeEvents myEvent, int Index) {
        ThingUID thingUID = findThingUID("NAWelcomeEvent", "Event#" + Index);
        Map<String, Object> properties = new HashMap<>(1);

        properties.put(org.openhab.binding.netatmo.NetatmoBindingConstants.WELCOME_HOME_ID, naWelcomeHome.getId());
        properties.put(org.openhab.binding.netatmo.NetatmoBindingConstants.WELCOME_EVENT_ID, "Event#" + Index);

        addDiscoveredThing(thingUID, properties, "Event " + Index);
    }

    private void addDiscoveredThing(ThingUID thingUID, Map<String, Object> properties, String displayLabel) {
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withBridge(netatmoBridgeHandler.getThing().getUID()).withLabel(displayLabel).build();

        thingDiscovered(discoveryResult);
    }

    private ThingUID findThingUID(String thingType, String thingId) throws IllegalArgumentException {
        for (ThingTypeUID supportedThingTypeUID : getSupportedThingTypes()) {
            String uid = supportedThingTypeUID.getId();

            if (uid.equalsIgnoreCase(thingType)) {

                return new ThingUID(supportedThingTypeUID, netatmoBridgeHandler.getThing().getUID(),
                        thingId.replaceAll("[^a-zA-Z0-9_]", ""));
            }
        }

        throw new IllegalArgumentException("Unsupported device type discovered :" + thingType);
    }

}
