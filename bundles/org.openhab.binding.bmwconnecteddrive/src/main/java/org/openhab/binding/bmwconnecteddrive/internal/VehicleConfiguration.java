/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;

/**
 * The {@link VehicleConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class VehicleConfiguration {
    /**
     * Vehicle Identification Number (VIN)
     */
    public String vin = Constants.EMPTY;

    /**
     * Data refresh rate in minutes
     */
    public int refreshInterval = ConnectedDriveConstants.DEFAULT_REFRESH_INTERVAL_MINUTES;

    /**
     * Either Auto Detect Miles units (UK & US) or select Format directly
     * <option value="AUTODETECT">Auto Detect</option>
     * <option value="METRIC">Metric</option>
     * <option value="IMPERIAL">Imperial</option>
     */
    public String units = ConnectedDriveConstants.UNITS_AUTODETECT;

    /**
     * image size - width & length (square)
     */
    public int imageSize = ConnectedDriveConstants.DEFAULT_IMAGE_SIZE_PX;

    /**
     * image viewport defined as options in thing xml
     * <option value="FRONT">Front</option>
     * <option value="REAR">Rear</option>
     * <option value="SIDE">Slide</option>
     * <option value="DASHBOARD">Dashboard</option>
     * <option value="DRIVERDOOR">Driver Door</option>
     */
    public String imageViewport = ConnectedDriveConstants.DEFAULT_IMAGE_VIEWPORT;
}
