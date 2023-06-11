/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.smaenergymeter.internal.configuration;

/**
 * The {@link EnergyMeterConfig} class holds the configuration properties of the binding.
 *
 * @author Osman Basha - Initial contribution
 */
public class EnergyMeterConfig {

    private String mcastGroup;
    private Integer port;
    private Integer pollingPeriod;

    public String getMcastGroup() {
        return mcastGroup;
    }

    public void setMcastGroup(String mcastGroup) {
        this.mcastGroup = mcastGroup;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getPollingPeriod() {
        return pollingPeriod;
    }

    public void setPollingPeriod(Integer pollingPeriod) {
        this.pollingPeriod = pollingPeriod;
    }
}
