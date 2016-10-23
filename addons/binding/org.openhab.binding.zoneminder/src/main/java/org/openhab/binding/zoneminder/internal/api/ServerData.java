/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.api;

/**
 * The Class ServerVersion Wraps JSON data from ZoneMinder API call.
 *
 * @author Martin S. Eskildsen
 */
public class ServerData extends ZoneMinderApiData {
    public String version;
    public String apiversion;
    private boolean daemonCheckState;

    public void setDaemonCheckState(boolean state) {
        daemonCheckState = state;
    }

    public boolean getDaemonCheckState(boolean state) {
        return daemonCheckState;
    }
}
