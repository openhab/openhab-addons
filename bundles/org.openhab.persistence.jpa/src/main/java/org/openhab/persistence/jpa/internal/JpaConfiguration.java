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
package org.openhab.persistence.jpa.internal;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The configuration required for Jpa binding.
 *
 * @author Manfred Bergmann - Initial contribution
 * @author Kai Kreuzer - migrated to 3.x
 *
 */
public class JpaConfiguration {
    private final Logger logger = LoggerFactory.getLogger(JpaConfiguration.class);

    private static final String CFG_CONNECTION_URL = "url";
    private static final String CFG_DRIVER_CLASS = "driver";
    private static final String CFG_USERNAME = "user";
    private static final String CFG_PASSWORD = "password";
    private static final String CFG_SYNCMAPPING = "syncmappings";

    public static boolean isInitialized = false;

    public final String dbConnectionUrl;
    public final String dbDriverClass;
    public final String dbUserName;
    public final String dbPassword;
    public final String dbSyncMapping;

    public JpaConfiguration(final Map<String, Object> properties) {
        logger.debug("Update config...");

        String param = (String) properties.get(CFG_CONNECTION_URL);
        logger.debug("url: {}", param);
        if (param == null) {
            logger.warn("Connection url is required in jpa.cfg!");
        } else if (param.isBlank()) {
            logger.warn("Empty connection url in jpa.cfg!");
        }
        dbConnectionUrl = param;

        param = (String) properties.get(CFG_DRIVER_CLASS);
        logger.debug("driver: {}", param);
        if (param == null) {
            logger.warn("Driver class is required in jpa.cfg!");
        } else if (param.isBlank()) {
            logger.warn("Empty driver class in jpa.cfg!");
        }
        dbDriverClass = param;

        if (properties.get(CFG_USERNAME) == null) {
            logger.info("{} was not specified!", CFG_USERNAME);
        }
        dbUserName = (String) properties.get(CFG_USERNAME);

        if (properties.get(CFG_PASSWORD) == null) {
            logger.info("{} was not specified!", CFG_PASSWORD);
        }
        dbPassword = (String) properties.get(CFG_PASSWORD);

        if (properties.get(CFG_SYNCMAPPING) == null) {
            logger.debug("{} was not specified!", CFG_SYNCMAPPING);
        }
        dbSyncMapping = (String) properties.get(CFG_SYNCMAPPING);

        isInitialized = true;
        logger.debug("Update config... done");
    }
}
