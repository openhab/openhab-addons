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
package org.openhab.binding.wifiled.internal.configuration;

/**
 * The {@link WiFiLEDConfig} class holds the configuration properties of the thing.
 *
 * @author Osman Basha - Initial contribution
 * @author Stefan Endrullis - Initial contribution
 */
public class WiFiLEDConfig {

    private String ip;
    private Integer port;
    private Integer pollingPeriod;
    private String protocol;
    private String driver;
    private Integer fadeDurationInMs;
    private Integer fadeSteps;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public Integer getFadeDurationInMs() {
        return fadeDurationInMs;
    }

    public void setFadeDurationInMs(Integer fadeDurationInMs) {
        this.fadeDurationInMs = fadeDurationInMs;
    }

    public Integer getFadeSteps() {
        return fadeSteps;
    }

    public void setFadeSteps(Integer fadeSteps) {
        this.fadeSteps = fadeSteps;
    }
}
