/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.manager;

import static org.openhab.binding.shelly.internal.manager.ShellyManagerConstants.IMAGE_PATH;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.substringAfter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.shelly.internal.ShellyHandlerFactory;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.handler.ShellyManagerInterface;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ShellyManagerImageLoader} implements the Shelly Manager's download proxy for images (load them from bundle)
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyManagerImageLoader extends ShellyManagerPage {
    private final Logger logger = LoggerFactory.getLogger(ShellyManagerImageLoader.class);

    public ShellyManagerImageLoader(ConfigurationAdmin configurationAdmin,
            ShellyTranslationProvider translationProvider, HttpClient httpClient, String localIp, int localPort,
            ShellyHandlerFactory handlerFactory) {
        super(configurationAdmin, translationProvider, httpClient, localIp, localPort, handlerFactory);
    }

    @Override
    public ShellyMgrResponse generateContent(String path, Map<String, String[]> parameters) throws ShellyApiException {
        return loadImage(substringAfter(path, ShellyManagerConstants.SHELLY_MGR_IMAGES_URI + "/"));
    }

    protected ShellyMgrResponse loadImage(String image) throws ShellyApiException {
        String file = IMAGE_PATH + image;
        logger.trace("Read Image from {}", file);
        ClassLoader cl = ShellyManagerInterface.class.getClassLoader();
        if (cl != null) {
            try (InputStream inputStream = cl.getResourceAsStream(file)) {
                if (inputStream != null) {
                    byte[] buf = new byte[inputStream.available()];
                    inputStream.read(buf);
                    return new ShellyMgrResponse(buf, HttpStatus.OK_200, "image/png");
                }
            } catch (IOException | RuntimeException e) {
                logger.debug("ShellyManager: Unable to read {} from bundle resources!", image, e);
            }
        }
        return new ShellyMgrResponse("Unable to read " + image + " from bundle resources!", HttpStatus.NOT_FOUND_404);
    }
}
