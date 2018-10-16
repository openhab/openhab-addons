/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.xmltv.handler;

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

    @Nullable
    private Tv currentXmlFile;

    @Nullable
    private ScheduledFuture<?> reloadJob = null;

    public XmlTVHandler(Bridge bridge) throws JAXBException {
        super(bridge);
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        jc = JAXBContext.newInstance(Tv.class.getPackage().getName());
    }

    @Override
    public void initialize() {
        XmlTVConfiguration config = getConfigAs(XmlTVConfiguration.class);
        logger.debug("Initializing {} for input file '{}'", getClass(), config.filePath);

        reloadJob = scheduler.scheduleWithFixedDelay(() -> {
            StreamSource source = new StreamSource(config.filePath);

            try {
                currentXmlFile = null;

                // This can take some seconds depending upon weight of the XmlTV source file
                XMLStreamReader xsr = xif.createXMLStreamReader(source);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                Tv xmlFile = (Tv) unmarshaller.unmarshal(xsr);

                // Remove all finished programmes
                xmlFile.getProgrammes().removeIf(programme -> Instant.now().isAfter(programme.getProgrammeStop()));

                // Sort programmes by starting instant
                Collections.sort(xmlFile.getProgrammes(), new Comparator<Programme>() {
                    @Override
                    public int compare(Programme programme2, Programme programme1) {
                        return programme2.getProgrammeStart().compareTo(programme1.getProgrammeStart());
                    }
                });

                // Ready to deliver data to ChannelHandlers
                currentXmlFile = xmlFile;
                updateStatus(ThingStatus.ONLINE);
            } catch (JAXBException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, e.getMessage());
            } catch (XMLStreamException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            }
        }, 1, config.refresh, TimeUnit.HOURS);
    }

    @SuppressWarnings("null")
    @Override
    public void dispose() {
        logger.debug("Running dispose()");
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
