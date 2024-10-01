/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * The {@link MyBMWVehicleConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - renaming and refactoring to Java Beans
 */
@NonNullByDefault
public class MyBMWVehicleConfiguration {
    /**
     * Vehicle Identification Number (VIN)
     */
    private String vin = Constants.EMPTY;

    /**
     * Vehicle brand
     * - bmw
     * - bmw_i
     * - mini
     */
    private String vehicleBrand = Constants.EMPTY;

    /**
     * Data refresh rate in minutes
     */
    private int refreshInterval = MyBMWConstants.DEFAULT_REFRESH_INTERVAL_MINUTES;

    /**
     * @return the vin
     */
    public String getVin() {
        return vin;
    }

    /**
     * @param vin the vin to set
     */
    public void setVin(String vin) {
        this.vin = vin;
    }

    /**
     * @return the vehicleBrand
     */
    public String getVehicleBrand() {
        return vehicleBrand;
    }

    /**
     * @param vehicleBrand the vehicleBrand to set
     */
    public void setVehicleBrand(String vehicleBrand) {
        this.vehicleBrand = vehicleBrand;
    }

    /**
     * @return the refreshInterval
     */
    public int getRefreshInterval() {
        return refreshInterval;
    }

    /**
     * @param refreshInterval the refreshInterval to set
     */
    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */

    @Override
    public String toString() {
        return "MyBMWVehicleConfiguration [vin=" + vin + ", vehicleBrand=" + vehicleBrand + ", refreshInterval="
                + refreshInterval + "]";
    }
}
