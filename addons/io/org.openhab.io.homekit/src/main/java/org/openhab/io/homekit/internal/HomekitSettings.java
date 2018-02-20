/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;

import org.osgi.framework.FrameworkUtil;

/**
 * Provides the configured and static settings for the Homekit addon
 *
 * @author Andy Lintner
 */
public class HomekitSettings {

    private static final String NAME = "openHAB";
    private static final String MANUFACTURER = "openHAB";
    private static final String SERIAL_NUMBER = "none";

    private int port = 9123;
    private String pin = "031-45-154";
    private boolean useFahrenheitTemperature = false;
    private double minimumTemperature = -100;
    private double maximumTemperature = 100;
    private String thermostatHeatMode = "HeatOn";
    private String thermostatCoolMode = "CoolOn";
    private String thermostatAutoMode = "Auto";
    private String thermostatOffMode = "Off";
    private InetAddress networkInterface;

    public void fill(Dictionary<String, ?> properties) throws UnknownHostException {
        Object port = properties.get("port");
        if (port instanceof Integer) {
            this.port = (Integer) port;
        } else if (port instanceof String) {
            String portString = (String) properties.get("port");
            if (portString != null) {
                this.port = Integer.parseInt(portString);
            }
        }
        this.pin = getOrDefault(properties.get("pin"), this.pin);
        Object useFahrenheitTemperature = properties.get("useFahrenheitTemperature");
        if (useFahrenheitTemperature instanceof Boolean) {
            this.useFahrenheitTemperature = (Boolean) useFahrenheitTemperature;
        } else if (useFahrenheitTemperature instanceof String) {
            String useFahrenheitTemperatureString = (String) properties.get("useFahrenheitTemperature");
            if (useFahrenheitTemperatureString != null) {
                this.useFahrenheitTemperature = Boolean.valueOf(useFahrenheitTemperatureString);
            }
        }
        Object minimumTemperature = properties.get("minimumTemperature");
        if (minimumTemperature != null) {
            this.minimumTemperature = Double.parseDouble(minimumTemperature.toString());
        }
        Object maximumTemperature = properties.get("maximumTemperature");
        if (maximumTemperature != null) {
            this.maximumTemperature = Double.parseDouble(maximumTemperature.toString());
        }
        this.thermostatHeatMode = (String) properties.get("thermostatHeatMode");
        this.thermostatCoolMode = (String) properties.get("thermostatCoolMode");
        this.thermostatAutoMode = (String) properties.get("thermostatAutoMode");
        this.thermostatOffMode = (String) properties.get("thermostatOffMode");

        String networkInterface = (String) properties.get("networkInterface");
        if (networkInterface == null) {
            this.networkInterface = InetAddress.getLocalHost();
        } else {
            this.networkInterface = InetAddress.getByName(networkInterface);
        }
    }

    private static String getOrDefault(Object value, String defaultValue) {
        return value != null ? (String) value : defaultValue;
    }

    public String getName() {
        return NAME;
    }

    public String getManufacturer() {
        return MANUFACTURER;
    }

    public String getSerialNumber() {
        return SERIAL_NUMBER;
    }

    public String getModel() {
        return FrameworkUtil.getBundle(getClass()).getVersion().toString();
    }

    public InetAddress getNetworkInterface() {
        return networkInterface;
    }

    public int getPort() {
        return port;
    }

    public String getPin() {
        return pin;
    }

    public boolean useFahrenheitTemperature() {
        return useFahrenheitTemperature;
    }

    public double getMaximumTemperature() {
        return maximumTemperature;
    }

    public double getMinimumTemperature() {
        return minimumTemperature;
    }

    public String getThermostatHeatMode() {
        return thermostatHeatMode;
    }

    public String getThermostatCoolMode() {
        return thermostatCoolMode;
    }

    public String getThermostatAutoMode() {
        return thermostatAutoMode;
    }

    public String getThermostatOffMode() {
        return thermostatOffMode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(maximumTemperature);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minimumTemperature);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((pin == null) ? 0 : pin.hashCode());
        result = prime * result + port;
        result = prime * result + ((thermostatAutoMode == null) ? 0 : thermostatAutoMode.hashCode());
        result = prime * result + ((thermostatCoolMode == null) ? 0 : thermostatCoolMode.hashCode());
        result = prime * result + ((thermostatHeatMode == null) ? 0 : thermostatHeatMode.hashCode());
        result = prime * result + ((thermostatOffMode == null) ? 0 : thermostatOffMode.hashCode());
        result = prime * result + (useFahrenheitTemperature ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HomekitSettings other = (HomekitSettings) obj;
        if (Double.doubleToLongBits(maximumTemperature) != Double.doubleToLongBits(other.maximumTemperature)) {
            return false;
        }
        if (Double.doubleToLongBits(minimumTemperature) != Double.doubleToLongBits(other.minimumTemperature)) {
            return false;
        }
        if (pin == null) {
            if (other.pin != null) {
                return false;
            }
        } else if (!pin.equals(other.pin)) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        if (thermostatAutoMode == null) {
            if (other.thermostatAutoMode != null) {
                return false;
            }
        } else if (!thermostatAutoMode.equals(other.thermostatAutoMode)) {
            return false;
        }
        if (thermostatCoolMode == null) {
            if (other.thermostatCoolMode != null) {
                return false;
            }
        } else if (!thermostatCoolMode.equals(other.thermostatCoolMode)) {
            return false;
        }
        if (thermostatHeatMode == null) {
            if (other.thermostatHeatMode != null) {
                return false;
            }
        } else if (!thermostatHeatMode.equals(other.thermostatHeatMode)) {
            return false;
        }
        if (thermostatOffMode == null) {
            if (other.thermostatOffMode != null) {
                return false;
            }
        } else if (!thermostatOffMode.equals(other.thermostatOffMode)) {
            return false;
        }
        if (useFahrenheitTemperature != other.useFahrenheitTemperature) {
            return false;
        }
        return true;
    }

}
