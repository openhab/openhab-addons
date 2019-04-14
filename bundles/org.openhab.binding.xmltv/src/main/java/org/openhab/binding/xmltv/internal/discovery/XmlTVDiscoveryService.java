/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.xmltv.internal.discovery;

import static org.openhab.binding.xmltv.internal.XmlTVBindingConstants.XMLTV_CHANNEL_THING_TYPE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.xmltv.internal.XmlTVBindingConstants;
import org.openhab.binding.xmltv.internal.configuration.XmlChannelConfiguration;
import org.openhab.binding.xmltv.internal.handler.XmlTVHandler;
import org.openhab.binding.xmltv.internal.jaxb.Tv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link XmlTVDiscoveryService} is responsible for discovering all channels
 * declared in the XmlTV file
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class XmlTVDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(XmlTVDiscoveryService.class);

    private static final int SEARCH_TIME = 10;

    private XmlTVHandler bridgeHandler;

    /**
     * Creates a XmlTVDiscoveryService with background discovery disabled.
     */
    public XmlTVDiscoveryService(XmlTVHandler bridgeHandler) {
        super(XmlTVBindingConstants.SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        logger.debug("Starting XmlTV discovery scan");
        if (bridgeHandler.getThing().getStatus() == ThingStatus.ONLINE) {
            Tv tv = bridgeHandler.getXmlFile();
            if (tv != null) {
                tv.getMediaChannels().stream().forEach(channel -> {
                    String channelId = channel.getId();
                    String uid = channelId.replaceAll("[^A-Za-z0-9_]", "_");
                    ThingUID thingUID = new ThingUID(XMLTV_CHANNEL_THING_TYPE, bridgeHandler.getThing().getUID(), uid);

                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                            .withBridge(bridgeHandler.getThing().getUID())
                            .withLabel(channel.getDisplayNames().get(0).getValue()).withRepresentationProperty(uid)
                            .withProperty(XmlChannelConfiguration.CHANNEL_ID, channelId).build();

                    thingDiscovered(discoveryResult);
                });
            }
        }
    }

}
