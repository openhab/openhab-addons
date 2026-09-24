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

        public RobonectVersion() {
            this(NOT_AVAILABLE, NOT_AVAILABLE, NOT_AVAILABLE, NOT_AVAILABLE);
        }

        public RobonectVersion(String serial, String version, String compiled, String comment) {
            this.serial = serial;
            this.version = version;
            this.compiled = compiled;
            this.comment = comment;
        }

        /**
         * @return - The serial number of the robonect module.
         */
        public String getSerial() {
            return serial;
        }

        /**
         * @return - The firmware version running on the robonect module.
         */
        public String getVersion() {
            return version;
        }

        /**
         * @return - The date and time the firmware was compiled.
         */
        public String getCompiled() {
            return compiled;
        }

        /**
         * @return - The comment added to this version.
         */
        public String getComment() {
            return comment;
        }

        public void setSerial(String serial) {
            this.serial = serial;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public void setCompiled(String compiled) {
            this.compiled = compiled;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }

    public @Nullable String getSerial() {
        return serial;
    }

    public @Nullable VersionMower getMower() {
        return mower;
    }

    public @Nullable VersionDetail getBootloader() {
        return bootloader;
    }

    public @Nullable VersionWlan getWlan() {
        return wlan;
    }

    public @Nullable VersionDetail getApplication() {
        return application;
    }

    private RobonectVersion robonect;

    /**
     * @return - the object encapsulating the version information. See {@link RobonectVersion}
     */
    public RobonectVersion getRobonect() {
        if (robonect != null) {
            return robonect;
        } else {
            return NA_VERSION;
        }
    }

    public void setRobonect(RobonectVersion robonect) {
        this.robonect = robonect;
    }
}
