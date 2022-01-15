/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.io.imperihome.internal;

import java.util.Map;

import org.openhab.core.id.InstanceUUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration parser and container.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class ImperiHomeConfig {

    private final Logger logger = LoggerFactory.getLogger(ImperiHomeConfig.class);

    private String systemId;
    private String rootUrl;

    public void update(Map<String, Object> config) {
        Object cSystemId = config.get("system.id");
        if (cSystemId == null || cSystemId.toString().isEmpty()) {
            systemId = InstanceUUID.get();
        } else {
            systemId = cSystemId.toString();
        }

        Object rootUrlObj = config.get("openhab.rootUrl");
        if (rootUrlObj != null) {
            rootUrl = String.valueOf(rootUrlObj);
            if (!rootUrl.endsWith("/")) {
                rootUrl += "/";
            }
        }

        logger.info("Configuration updated");
    }

    public String getSystemId() {
        return systemId;
    }

    public String getRootUrl() {
        return rootUrl;
    }
}
