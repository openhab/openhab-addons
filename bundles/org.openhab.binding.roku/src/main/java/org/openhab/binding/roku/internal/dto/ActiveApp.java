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
package org.openhab.binding.roku.internal.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Maps the XML response from the Roku HTTP endpoint '/query/active-app' (Active app info)
 *
 * @author Michael Lobstein - Initial contribution
 */

@NonNullByDefault
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "active-app")
public class ActiveApp {
    @XmlElement
    private ActiveApp.App app = new App();

    @XmlElement
    private ActiveApp.Screensaver screensaver = new Screensaver();

    public ActiveApp.App getApp() {
        return app;
    }

    public void setApp(ActiveApp.App value) {
        this.app = value;
    }

    public ActiveApp.Screensaver getScreensaver() {
        return screensaver;
    }

    public void setScreensaver(ActiveApp.Screensaver value) {
        this.screensaver = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class App {
        @XmlValue
        private String value = "";

        @XmlAttribute(name = "id")
        private String id = "-1";

        @XmlAttribute(name = "type")
        private String type = "";

        @XmlAttribute(name = "version")
        private String version = "";

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getId() {
            return id;
        }

        public void setId(String value) {
            this.id = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String value) {
            this.type = value;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String value) {
            this.version = value;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Screensaver {
        @XmlValue
        private String value = "";

        @XmlAttribute(name = "id")
        private int id = -1;

        @XmlAttribute(name = "type")
        private String type = "";

        @XmlAttribute(name = "version")
        private String version = "";

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getId() {
            return id;
        }

        public void setId(int value) {
            this.id = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String value) {
            this.type = value;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String value) {
            this.version = value;
        }
    }
}
