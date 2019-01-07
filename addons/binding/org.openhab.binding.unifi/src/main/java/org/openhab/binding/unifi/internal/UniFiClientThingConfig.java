/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.unifi.internal.handler.UniFiClientThingHandler;

/**
 * The {@link UniFiClientThingConfig} encapsulates all the configuration options for an instance of the
 * {@link UniFiClientThingHandler}.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiClientThingConfig {

    private String cid;

    private String site;

    private int considerHome = 180;

    public String getClientID() {
        return cid;
    }

    public String getSite() {
        return site;
    }

    public int getConsiderHome() {
        return considerHome;
    }

    public UniFiClientThingConfig tidy() {
        cid = StringUtils.lowerCase(StringUtils.strip(cid));
        site = StringUtils.lowerCase(StringUtils.strip(site));
        return this;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(cid);
    }

    @Override
    public String toString() {
        return String.format("UniFiClientConfig{cid: '%s', site: '%s', considerHome: %d}", cid, site, considerHome);
    }

}
