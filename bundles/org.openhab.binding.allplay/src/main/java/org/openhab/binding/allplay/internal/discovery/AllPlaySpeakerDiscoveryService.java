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
package org.openhab.binding.allplay.internal.discovery;

import static org.openhab.binding.allplay.internal.AllPlayBindingConstants.SPEAKER_THING_TYPE;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.allplay.internal.AllPlayBindingConstants;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
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
@Component(service = DiscoveryService.class, configurationPid = "discovery.allplay")
public class AllPlaySpeakerDiscoveryService extends AbstractDiscoveryService implements SpeakerAnnouncedListener {

    private final Logger logger = LoggerFactory.getLogger(AllPlaySpeakerDiscoveryService.class);

    private static final int DISCOVERY_TIMEOUT = 30;
    private static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Set.of(SPEAKER_THING_TYPE);
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
