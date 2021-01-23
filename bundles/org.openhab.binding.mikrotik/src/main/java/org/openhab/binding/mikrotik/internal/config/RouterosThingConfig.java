/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.mikrotik.internal.config;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RouterosThingConfig} class contains fields mapping thing configuration parameters.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class RouterosThingConfig {
    public String host = "rb3011";
    public int port = 8728;
    public String login = "admin";
    public String password = "";
    public int refresh = 10;

    public boolean isValid() {
        return StringUtils.isNotBlank(host) && StringUtils.isNotBlank(login) && StringUtils.isNotBlank(password);
    }

    @Override
    public String toString() {
        return String.format("RouterosThingConfig{host=%s, port=%d, login=%s, password=*****, refresh=%ds}",
                host, port, login, refresh);
    }

}
