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
package org.openhab.binding.xmltv.internal.handler;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.xmltv.internal.configuration.XmlTVConfiguration;
import org.openhab.binding.xmltv.internal.jaxb.Programme;
import org.openhab.binding.xmltv.internal.jaxb.Tv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link XmlTVHandler} is responsible for handling XMLTV file and dispatch
 * information made available to according Media Channels
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class XmlTVHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(XmlTVHandler.class);
    private final XMLInputFactory xif = XMLInputFactory.newFactory();
    private final JAXBContext jc;

    private @Nullable Tv currentXmlFile;
    private @NonNullByDefault({}) ScheduledFuture<?> reloadJob;

    public XmlTVHandler(Bridge thing) throws JAXBException {
        super(thing);
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        jc = JAXBContext.newInstance(Tv.class);
    }

    @Override
    public void initialize() {
        XmlTVConfiguration config = getConfigAs(XmlTVConfiguration.class);
        logger.debug("Initializing {} for input file '{}'", getClass(), config.filePath);

        reloadJob = scheduler.scheduleWithFixedDelay(() -> {
            final StreamSource source = new StreamSource(config.filePath);
            currentXmlFile = null;
            XMLStreamReader xsr = null;
            try {
                // This can take some seconds depending upon weight of the XmlTV source file
                xsr = xif.createXMLStreamReader(source);

                try {
                    Unmarshaller unmarshaller = jc.createUnmarshaller();
                    Tv xmlFile = (Tv) unmarshaller.unmarshal(xsr);
                    // Remove all finished programmes
                    xmlFile.getProgrammes().removeIf(programme -> Instant.now().isAfter(programme.getProgrammeStop()));

                    if (xmlFile.getProgrammes().size() > 0) {
                        // Sort programmes by starting instant
                        Collections.sort(xmlFile.getProgrammes(), Comparator.comparing(Programme::getProgrammeStart));
                        // Ready to deliver data to ChannelHandlers
                        currentXmlFile = xmlFile;
                        updateStatus(ThingStatus.ONLINE);
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DISABLED, "XMLTV file seems outdated");
                    }
                    xsr.close();
                } catch (JAXBException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, e.getMessage());
                }
            } catch (XMLStreamException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            } finally {
                try {
                    if (xsr != null) {
                        xsr.close();
                    }
                } catch (XMLStreamException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                }
            }
        }, 0, config.refresh, TimeUnit.HOURS);
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose");
        if (reloadJob != null && !reloadJob.isCancelled()) {
            reloadJob.cancel(true);
            reloadJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // nothing to do
    }

    @Nullable
    public Tv getXmlFile() {
        return currentXmlFile;
    }

}
