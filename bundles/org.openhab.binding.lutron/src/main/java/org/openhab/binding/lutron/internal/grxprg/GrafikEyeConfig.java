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
package org.openhab.binding.lutron.internal.grxprg;

/**
 * Configuration class for the Grafik Eye controlled by the PRG interface
 *
 * @author Tim Roberts - Initial contribution
 */
public class GrafikEyeConfig {
    /**
     * The control unit identifier
     */
    private int controlUnit;

    /**
     * The default fade for the unit
     */
    private int fade;

    /**
     * The zones designated as shades as parsed
     */
    private boolean[] shades = new boolean[8];

    /**
     * A string representing if the shade configuration was invalid. Will be null if valid, not-null if invalid
     */
    private String shadeError;

    /**
     * Polling time (in seconds) to refresh state for the unit.
     */
    private int polling;

    /**
     * Validates the configuration. Ensures the control unit. fade and shadeError are valid.
     *
     * @return a non-null text if invalid (explaining why), a null if valid
     */
    String validate() {
        if (controlUnit < 1 || controlUnit > 8) {
            return "controlUnit must be between 1-8";
        }

        if (fade < 0 || fade > 3600) {
            return "fade must be between 0-3600";
        }

        if (shadeError != null) {
            return shadeError;
        }
        return null;
    }

    /**
     * Returns the Control Unit identifier
     *
     * @return the control unit identifier
     */
    public int getControlUnit() {
        return controlUnit;
    }

    /**
     * Sets the control unit identifier
     *
     * @param controlUnit the control unit identifier
     */
    public void setControlUnit(int controlUnit) {
        this.controlUnit = controlUnit;
    }

    /**
     * Returns the default fade
     *
     * @return the default fade
     */
    public int getFade() {
        return fade;
    }

    /**
     * Sets the default fade
     *
     * @param fade the default fade
     */
    public void setFade(int fade) {
        this.fade = fade;
    }

    /**
     * Helper method to determine if the zone is a shade zone or not. If zone number is invalid, false will be returned.
     *
     * @param zone the zone number
     * @return true if designated as a shade, false otherwise
     */
    boolean isShadeZone(int zone) {
        if (zone >= 1 && zone <= shades.length) {
            return shades[zone - 1];
        }
        return false;
    }

    /**
     * Returns a comma formatted list of shade zones
     *
     * @returna non-null, non-empty comma delimited list of shade zones
     */
    public String getShadeZones() {
        final StringBuilder sb = new StringBuilder();
        for (int z = 0; z < shades.length; z++) {
            if (shades[z]) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append((z + 1));
            }
        }
        return sb.toString();
    }

    /**
     * Sets the shade zones from a comma delimited list (ex: "2,3,4")
     *
     * @param shadeZones a, possibly null, list of zones
     */
    public void setShadeZones(String shadeZones) {
        shadeError = null;

        for (int zone = 0; zone < 8; zone++) {
            shades[zone] = false;
        }

        if (shadeZones != null) {
            for (String shadeZone : shadeZones.split(",")) {
                try {
                    final int zone = Integer.parseInt(shadeZone);
                    if (zone >= 1 && zone <= 8) {
                        shades[zone - 1] = true;
                    } else {
                        shadeError = "Shade zone must be between 1-8: " + zone + " - ignoring";
                    }
                } catch (NumberFormatException e) {
                    shadeError = "Unknown shade zone (can't parse to numeric): " + shadeZone + " - ignoring";
                }
            }
        }
    }

    /**
     * Gets the polling (in seconds) to refresh state
     *
     * @return the polling (in seconds) to refresh state
     */
    public int getPolling() {
        return polling;
    }

    /**
     * Sets the polling (in seconds) to refresh state
     *
     * @param polling the polling (in seconds) to refresh state
     */
    public void setPolling(int polling) {
        this.polling = polling;
    }
}
