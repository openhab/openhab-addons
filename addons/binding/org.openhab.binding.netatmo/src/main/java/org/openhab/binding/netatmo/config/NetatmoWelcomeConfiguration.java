/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.config;

/**
 * The {@link NetatmoWelcomeConfiguration} is responsible for holding configuration
 * informations needed to access a Netatmo Welcome Camera
 *
 * @author Ing. Peter Weiss - Initial contribution
 */
public class NetatmoWelcomeConfiguration {

    private String welcomeHomeId;
    private String welcomePersonId;
    private String welcomeCameraId;
    private String welcomeEventId;

    public long refreshInterval;

    public String getWelcomeHomeId() {
        return welcomeHomeId == null ? null : welcomeHomeId.toLowerCase();
    }

    public String getWelcomePersonId() {
        return welcomePersonId == null ? null : welcomePersonId.toLowerCase();
    }

    public String getWelcomeCameraId() {
        return welcomeCameraId == null ? null : welcomeCameraId.toLowerCase();
    }

    public String getWelcomeEventId() {
        return welcomeEventId == null ? null : welcomeEventId.toLowerCase();
    }

}
