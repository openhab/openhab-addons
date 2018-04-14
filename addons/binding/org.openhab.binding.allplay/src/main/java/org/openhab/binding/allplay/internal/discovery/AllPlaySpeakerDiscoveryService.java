/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.allplay.internal.discovery;

import static org.openhab.binding.allplay.AllPlayBindingConstants.SPEAKER_THING_TYPE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.allplay.AllPlayBindingConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kaizencode.tchaikovsky.AllPlay;
import de.kaizencode.tchaikovsky.exception.AllPlayException;
import de.kaizencode.tchaikovsky.listener.SpeakerAnnouncedListener;
import de.kaizencode.tchaikovsky.speaker.Speaker;

/**
 * Discovery service to scan for AllPlay devices.
 *
 * @author Dominic Lerbs - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.allplay")
public class AllPlaySpeakerDiscoveryService extends AbstractDiscoveryService implements SpeakerAnnouncedListener {

    private final Logger logger = LoggerFactory.getLogger(AllPlaySpeakerDiscoveryService.class);

    private static final int DISCOVERY_TIMEOUT = 30;
    private static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections.singleton(SPEAKER_THING_TYPE);
    private AllPlay allPlay;

    public AllPlaySpeakerDiscoveryService() {
        super(DISCOVERABLE_THING_TYPES_UIDS, DISCOVERY_TIMEOUT);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting scan for AllPlay devices");
        try {
            allPlay = new AllPlay("openHAB2-discovery");
            allPlay.addSpeakerAnnouncedListener(this);
            allPlay.connect();
            allPlay.discoverSpeakers();
        } catch (AllPlayException e) {
            logger.warn("Error while scanning for AllPlay devices", e);
        }
    }

    @Override
    protected void stopScan() {
        logger.debug("Stopping scan for AllPlay devices");
        if (allPlay != null) {
            allPlay.removeSpeakerAnnouncedListener(this);
            allPlay.disconnect();
        }

        super.stopScan();
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.trace("Starting background scan for AllPlay devices");
        startScan();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.trace("Stopping background scan for AllPlay devices");
        stopScan();
    }

    @Override
    public void deactivate() {
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void onSpeakerAnnounced(Speaker speaker) {
        logger.debug("Speaker {} found by discovery service", speaker);
        ThingUID thingUID = new ThingUID(AllPlayBindingConstants.SPEAKER_THING_TYPE, speaker.getId());

        Map<String, Object> properties = new HashMap<>();
        properties.put(AllPlayBindingConstants.DEVICE_ID, speaker.getId());
        properties.put(AllPlayBindingConstants.DEVICE_NAME, speaker.getName());

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withRepresentationProperty(AllPlayBindingConstants.DEVICE_ID).withLabel(speaker.getName()).build();
        thingDiscovered(discoveryResult);

    }

}
