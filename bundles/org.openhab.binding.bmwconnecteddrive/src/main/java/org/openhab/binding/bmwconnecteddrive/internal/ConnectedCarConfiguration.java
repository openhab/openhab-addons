/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ConnectedCarConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ConnectedCarConfiguration {
    /**
     * Vehilce Identification Number (VIN)
     */
    public String vin = "";

    /**
     * Data refresh rate in minutes
     */
    public int refreshInterval = 15;

    /**
     * Either Auto Detect Miles units (UK & US) or select Format directly
     * <option value="AUTODETECT">Auto Detect</option>
     * <option value="METRIC">Metric</option>
     * <option value="IMPERIAL">Imperial</option>
     */
    public String units = "AUTODETECT";

    /**
     * image size - width & length (square)
     */
    public int imageSize = 300;

    /**
     * image viewport defined as options in thing xml
     * <option value="FRONTSIDE">Frontside</option>
     * <option value="FRONT">Front</option>
     * <option value="REARSIDE">Rearside</option>
     * <option value="REAR">Rear</option>
     * <option value="SIDE">Slide</option>
     * <option value="DASHBOARD">Dashboard</option>
     * <option value="DRIVERDOOR">Driver Door</option>
     * <option value="REARBIRDSEYE">Rear BirdsEye</option>
     */
    public String imageViewport = "FRONT";
}
