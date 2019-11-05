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
package org.openhab.binding.rachio.internal;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RachioConfiguration} contains the binding configuration and default values. The field names represent the
 * configuration names, do not rename them if you don't intend to break the configuration interface.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class RachioConfiguration {
    private final Logger       logger            = LoggerFactory.getLogger(RachioConfiguration.class);

    public static final String ERR_APIKEY        = "ERROR: No/invalid APIKEY in configuration, check services/rachio.cfg";

    public String              apikey            = "";
    public int                 pollingInterval   = DEFAULT_POLLING_INTERVAL;
    public int                 defaultRuntime    = DEFAULT_ZONE_RUNTIME;
    public String              callbackUrl       = "";
    public Boolean             clearAllCallbacks = false;
    public String              ipFilter          = "192.168.0.0/16;10.0.0.0/8;172.16.0.0/12";

    public void updateConfig(@Nullable Map<String, @Nullable Object> config) {
        Validate.notNull(config);
        for (HashMap.@Nullable Entry<String, @Nullable Object> ce : config.entrySet()) {
            Validate.notNull(ce);
            @Nullable
            String key = ce.getKey();
            Validate.notNull(key);
            Validate.notNull(ce.getValue());
            @SuppressWarnings("null")
            String value = ce.getValue().toString();
            if (key.equalsIgnoreCase("component.name") || key.equalsIgnoreCase("component.id")) {
                continue;
            }

            if (key.equalsIgnoreCase("service.pid")) {
                logger.debug("Rachio: Binding configuration:");
            }
            logger.debug("  {}={}", key, value);

            if (key.equalsIgnoreCase(PARAM_APIKEY)) {
                apikey = value;
            } else if (key.equalsIgnoreCase(PARAM_POLLING_INTERVAL)) {
                this.pollingInterval = Integer.parseInt(value);
            } else if (key.equalsIgnoreCase(PARAM_DEF_RUNTIME)) {
                this.defaultRuntime = Integer.parseInt(value);
            } else if (key.equalsIgnoreCase(PARAM_CALLBACK_URL)) {
                this.callbackUrl = value;
            } else if (key.equalsIgnoreCase(PARAM_IPFILTER)) {
                this.ipFilter = value;
            } else if (key.equalsIgnoreCase(PARAM_CLEAR_CALLBACK)) {
                String str = value;
                this.clearAllCallbacks = str.toLowerCase().equals("true");
            }
        }
    }
}
