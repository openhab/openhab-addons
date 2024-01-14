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
package org.openhab.binding.xmltv.internal.discovery;

import static org.openhab.binding.xmltv.internal.XmlTVBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.xmltv.internal.configuration.XmlChannelConfiguration;
import org.openhab.binding.xmltv.internal.handler.XmlTVHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link XmlTVDiscoveryService} is responsible for discovering all channels
 * declared in the XmlTV file
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = XmlTVDiscoveryService.class)
@NonNullByDefault
public class XmlTVDiscoveryService extends AbstractThingHandlerDiscoveryService<XmlTVHandler> {
    private static final int SEARCH_TIME = 5;

    private final Logger logger = LoggerFactory.getLogger(XmlTVDiscoveryService.class);

    /**
     * Creates a XmlTVDiscoveryService with background discovery disabled.
     */
    @Activate
    public XmlTVDiscoveryService() {
        super(XmlTVHandler.class, SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting XmlTV discovery scan");
        if (thingHandler.getThing().getStatus() == ThingStatus.ONLINE) {
            thingHandler.getXmlFile().ifPresent(tv -> {
                tv.getMediaChannels().stream().forEach(channel -> {
                    String channelId = channel.getId();
                    String uid = channelId.replaceAll("[^A-Za-z0-9_]", "_");
                    ThingUID thingUID = new ThingUID(XMLTV_CHANNEL_THING_TYPE, thingHandler.getThing().getUID(), uid);

                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                            .withBridge(thingHandler.getThing().getUID())
                            .withLabel(channel.getDisplayNames().get(0).getValue()).withRepresentationProperty(uid)
                            .withProperty(XmlChannelConfiguration.CHANNEL_ID, channelId).build();

                    thingDiscovered(discoveryResult);
                });
            });
        }
    }
}
