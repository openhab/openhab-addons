/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.harmonyhub.discovery;

/**
 * The {@link HarmonyHubDiscoveryResult} class represents a discovery result obtained from network discovery of a
 * Harmony Hub
 *
 * @author Dan Cunningham - Initial contribution
 *
 */
public class HarmonyHubDiscoveryResult {
    private String host;
    private String id;
    private String friendlyName;

    public HarmonyHubDiscoveryResult(String host, String id, String friendlyName) {
        super();
        this.host = host;
        this.id = id;
        this.friendlyName = friendlyName;
    }

    public String getHost() {
        return host;
    }

    public String getId() {
        return id;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

}