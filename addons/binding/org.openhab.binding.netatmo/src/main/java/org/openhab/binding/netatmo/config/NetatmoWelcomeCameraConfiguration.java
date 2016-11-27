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
 * The {@link NetatmoWelcomeCameraConfiguration} is responsible for holding configuration
 * informations needed to access a Netatmo Welcome Camera
 *
 * @author Ing. Peter Weiss - Initial contribution
 */
public class NetatmoWelcomeCameraConfiguration extends NetatmoWelcomeHomeConfiguration {

    @Override
    public String getId() {
        return getWelcomeCameraId();
    }

    @Override
    public String getParentId() {
        return getWelcomeHomeId();
    }

}
