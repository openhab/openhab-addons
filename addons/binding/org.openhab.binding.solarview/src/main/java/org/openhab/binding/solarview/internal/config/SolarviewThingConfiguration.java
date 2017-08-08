/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solarview.internal.config;

import org.eclipse.smarthome.config.core.Configuration;

/**
 * The {@link SolarviewThingConfiguration} is a wrapper for
 * configuration settings needed to access the <B>Solarview</B> device to be used for method
 * {@link org.openhab.binding.solarview.handler.SolarviewHandler#initialize()}.
 * <p>
 * It contains the factory default values as well.
 * <p>
 *
 * @author Guenther Schreiner - Initial contribution
 */
public class SolarviewThingConfiguration extends Configuration {

    public static final String THING_REFRESH_SECS = "refreshSecs";

    public int refreshSecs;

    /**
     * Default values - should not be modified
     */
    public SolarviewThingConfiguration() {
        refreshSecs = 60;
    }

}
/*
 * end-of-internal/config/SolarviewThingConfiguration.java
 */
