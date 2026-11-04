/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Answer object for holding version information.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class VersionInfo extends RobonectAnswer {

    public static final String NOT_AVAILABLE = "n/a";

    private static final RobonectVersion NA_VERSION = new RobonectVersion();

    private @Nullable String serial;
    private @Nullable VersionMower mower;
    private @Nullable VersionDetail bootloader;
    private @Nullable VersionWlan wlan;
    private @Nullable VersionDetail application;

    /**
     * encapsulates the robonect version information.
     */
    public static class RobonectVersion {

        private String serial;
        private String version;
        private String compiled;
        private String comment;

        /**
         * Creates Robonect module version information.
         */
        public RobonectVersion() {
            this(NOT_AVAILABLE, NOT_AVAILABLE, NOT_AVAILABLE, NOT_AVAILABLE);
        }

        /**
         * Creates Robonect module version information.
         *
         * @param serial the serial
         * @param version the version
         * @param compiled the compilation timestamp
         * @param comment the comment
         */
        public RobonectVersion(String serial, String version, String compiled, String comment) {
            this.serial = serial;
            this.version = version;
            this.compiled = compiled;
            this.comment = comment;
        }

        /**
         * Returns the serial number of the robonect module.
         *
         * @return - The serial number of the robonect module.
         */
        public String getSerial() {
            return serial;
        }

        /**
         * Returns the firmware version running on the robonect module.
         *
         * @return - The firmware version running on the robonect module.
         */
        public String getVersion() {
            return version;
        }

        /**
         * Returns the date and time the firmware was compiled.
         *
         * @return - The date and time the firmware was compiled.
         */
        public String getCompiled() {
            return compiled;
        }

        /**
         * Returns the comment added to this version.
         *
         * @return - The comment added to this version.
         */
        public String getComment() {
            return comment;
        }

        /**
         * Sets the serial.
         *
         * @param serial the serial
         */
        public void setSerial(String serial) {
            this.serial = serial;
        }

        /**
         * Sets the version.
         *
         * @param version the version
         */
        public void setVersion(String version) {
            this.version = version;
        }

        /**
         * Sets the compilation timestamp.
         *
         * @param compiled the compilation timestamp
         */
        public void setCompiled(String compiled) {
            this.compiled = compiled;
        }

        /**
         * Sets the comment.
         *
         * @param comment the comment
         */
        public void setComment(String comment) {
            this.comment = comment;
        }
    }

    /**
     * Returns the serial.
     *
     * @return the serial, or {@code null} if not reported
     */
    public @Nullable String getSerial() {
        return serial;
    }

    /**
     * Returns the mower.
     *
     * @return the mower, or {@code null} if not reported
     */
    public @Nullable VersionMower getMower() {
        return mower;
    }

    /**
     * Returns the bootloader.
     *
     * @return the bootloader, or {@code null} if not reported
     */
    public @Nullable VersionDetail getBootloader() {
        return bootloader;
    }

    /**
     * Returns the wlan.
     *
     * @return the wlan, or {@code null} if not reported
     */
    public @Nullable VersionWlan getWlan() {
        return wlan;
    }

    /**
     * Returns the application.
     *
     * @return the application, or {@code null} if not reported
     */
    public @Nullable VersionDetail getApplication() {
        return application;
    }

    private RobonectVersion robonect;

    /**
     * Returns the Robonect module version information, or the unavailable-value placeholder if absent.
     *
     * @return the Robonect module version information, or the unavailable-value placeholder if absent
     */
    public RobonectVersion getRobonect() {
        if (robonect != null) {
            return robonect;
        } else {
            return NA_VERSION;
        }
    }

    /**
     * Sets the robonect.
     *
     * @param robonect the robonect
     */
    public void setRobonect(RobonectVersion robonect) {
        this.robonect = robonect;
    }
}
