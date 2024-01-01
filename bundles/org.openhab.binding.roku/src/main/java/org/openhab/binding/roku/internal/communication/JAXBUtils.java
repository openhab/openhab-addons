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
package org.openhab.binding.roku.internal.communication;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLInputFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.roku.internal.dto.ActiveApp;
import org.openhab.binding.roku.internal.dto.Apps;
import org.openhab.binding.roku.internal.dto.DeviceInfo;
import org.openhab.binding.roku.internal.dto.Player;
import org.openhab.binding.roku.internal.dto.TvChannel;
import org.openhab.binding.roku.internal.dto.TvChannels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation for a static use of JAXBContext as singleton instance.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class JAXBUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JAXBUtils.class);

    public static final @Nullable JAXBContext JAXBCONTEXT_ACTIVE_APP = initJAXBContextActiveApp();
    public static final @Nullable JAXBContext JAXBCONTEXT_APPS = initJAXBContextApps();
    public static final @Nullable JAXBContext JAXBCONTEXT_DEVICE_INFO = initJAXBContextDeviceInfo();
    public static final @Nullable JAXBContext JAXBCONTEXT_PLAYER = initJAXBContextPlayer();
    public static final @Nullable JAXBContext JAXBCONTEXT_TVCHANNEL = initJAXBContextTvChannel();
    public static final @Nullable JAXBContext JAXBCONTEXT_TVCHANNELS = initJAXBContextTvChannels();
    public static final XMLInputFactory XMLINPUTFACTORY = initXMLInputFactory();

    private static @Nullable JAXBContext initJAXBContextActiveApp() {
        try {
            return JAXBContext.newInstance(ActiveApp.class);
        } catch (JAXBException e) {
            LOGGER.error("Exception creating JAXBContext for active app: {}", e.getLocalizedMessage(), e);
            return null;
        }
    }

    private static @Nullable JAXBContext initJAXBContextApps() {
        try {
            return JAXBContext.newInstance(Apps.class);
        } catch (JAXBException e) {
            LOGGER.error("Exception creating JAXBContext for app list: {}", e.getLocalizedMessage(), e);
            return null;
        }
    }

    private static @Nullable JAXBContext initJAXBContextDeviceInfo() {
        try {
            return JAXBContext.newInstance(DeviceInfo.class);
        } catch (JAXBException e) {
            LOGGER.error("Exception creating JAXBContext for device info: {}", e.getLocalizedMessage(), e);
            return null;
        }
    }

    private static @Nullable JAXBContext initJAXBContextPlayer() {
        try {
            return JAXBContext.newInstance(Player.class);
        } catch (JAXBException e) {
            LOGGER.error("Exception creating JAXBContext for player info: {}", e.getLocalizedMessage(), e);
            return null;
        }
    }

    private static @Nullable JAXBContext initJAXBContextTvChannel() {
        try {
            return JAXBContext.newInstance(TvChannel.class);
        } catch (JAXBException e) {
            LOGGER.error("Exception creating JAXBContext for TvChannel info: {}", e.getLocalizedMessage(), e);
            return null;
        }
    }

    private static @Nullable JAXBContext initJAXBContextTvChannels() {
        try {
            return JAXBContext.newInstance(TvChannels.class);
        } catch (JAXBException e) {
            LOGGER.error("Exception creating JAXBContext for TvChannels info: {}", e.getLocalizedMessage(), e);
            return null;
        }
    }

    private static XMLInputFactory initXMLInputFactory() {
        XMLInputFactory xif = XMLInputFactory.newInstance();
        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        return xif;
    }
}
