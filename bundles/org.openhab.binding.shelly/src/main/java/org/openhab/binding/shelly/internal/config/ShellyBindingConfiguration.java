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
package org.openhab.binding.shelly.internal.config;

import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ShellyBindingConfiguration} class contains fields mapping binding configuration parameters.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyBindingConfiguration {
    // Binding Configuration Properties
    public static final String CONFIG_DEF_HTTP_USER = "defaultUserId";
    public static final String CONFIG_DEF_HTTP_PWD  = "defaultPassword";

    public String              defaultUserId        = "";               // default for http basic user id
    public String              defaultPassword      = "";               // default for http basic auth password

    public Integer             httpPort             = 0;
    public String              localIp              = "";

    public void updateFromProperties(Map<String, Object> properties) {
        Validate.notNull(properties);

        for (Map.Entry<String, Object> e : properties.entrySet()) {
            switch (e.getKey()) {
                case CONFIG_DEF_HTTP_USER:
                    defaultUserId = (String) e.getValue();
                    break;
                case CONFIG_DEF_HTTP_PWD:
                    defaultPassword = (String) e.getValue();
                    break;
            }

        }
    }

    public void updateFromProperties(Dictionary<String, Object> properties) {
        Validate.notNull(properties);
        List<String> keys = Collections.list(properties.keys());
        Map<String, Object> dictCopy = keys.stream().collect(Collectors.toMap(Function.identity(), properties::get));
        updateFromProperties(dictCopy);
    }
}
