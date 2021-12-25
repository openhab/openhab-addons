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
package org.openhab.binding.mybmw.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mybmw.internal.utils.Constants;

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
     * Vehicle brand
     * - bmw
     * - mini
     */
    public String brand = Constants.EMPTY;

    /**
     * Data refresh rate in minutes
     */
    public int refreshInterval = MyBMWConstants.DEFAULT_REFRESH_INTERVAL_MINUTES;

    /**
     * image size - width & length (square)
     */
    public int imageSize = MyBMWConstants.DEFAULT_IMAGE_SIZE_PX;

    /**
     * image viewport defined as options in thing xml
     * <option value="FRONT">Front</option>
     * <option value="REAR">Rear</option>
     * <option value="SIDE">Slide</option>
     * <option value="DASHBOARD">Dashboard</option>
     * <option value="DRIVERDOOR">Driver Door</option>
     */
    public String imageViewport = MyBMWConstants.DEFAULT_IMAGE_VIEWPORT;
}
