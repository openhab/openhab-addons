/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.model;

/**
 * @author Marco Meyer - Initial contribution
 */
public class VersionInfo extends RobonectAnswer {

    public class RobonectVersion {
        private String serial;

        private String version;

        private String compiled;

        private String comment;

        public String getSerial() {
            return serial;
        }

        public String getVersion() {
            return version;
        }

        public String getCompiled() {
            return compiled;
        }

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

    public RobonectVersion getRobonect() {
        return robonect;
    }

    public void setRobonect(RobonectVersion robonect) {
        this.robonect = robonect;
    }
}
