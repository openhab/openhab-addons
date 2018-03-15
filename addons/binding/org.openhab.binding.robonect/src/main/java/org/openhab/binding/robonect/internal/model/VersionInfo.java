/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.internal.model;

/**
 * Answer object for holding version information.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class VersionInfo extends RobonectAnswer {

    /**
     * encapsulates the robonect version information.
     */
    public class RobonectVersion {
        private String serial;

        private String version;

        private String compiled;

        private String comment;

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
    
    private RobonectVersion robonect;

    /**
     * @return - the object encapsulating the version information. See {@link RobonectVersion}
     */
    public RobonectVersion getRobonect() {
        return robonect;
    }

    public void setRobonect(RobonectVersion robonect) {
        this.robonect = robonect;
    }
}
