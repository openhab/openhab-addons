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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Maps the XML response from the Roku HTTP endpoint '/query/apps' (List of installed apps)
 *
 * @author Michael Lobstein - Initial contribution
 */

@NonNullByDefault
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "apps")
public class Apps {
    @XmlElement
    private List<Apps.App> app = new ArrayList<>();

    public List<Apps.App> getApp() {
        return this.app;
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
}
