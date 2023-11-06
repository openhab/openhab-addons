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
package org.openhab.binding.nuvo.internal.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Create a Java object tree that represents the Nuvo keypad menu structure defined by the user
 *
 * @author Michael Lobstein - Initial contribution
 */

@NonNullByDefault
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "menu")
public class NuvoMenu {

    @XmlElement(required = true)
    protected List<NuvoMenu.Source> source = new ArrayList<NuvoMenu.Source>();

    public List<NuvoMenu.Source> getSource() {
        return this.source;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Source {
        protected List<NuvoMenu.Source.TopMenu> topmenu = new ArrayList<NuvoMenu.Source.TopMenu>();

        public List<NuvoMenu.Source.TopMenu> getTopMenu() {
            return this.topmenu;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class TopMenu {
            protected List<String> item = new ArrayList<String>();
            @XmlAttribute(name = "text", required = true)
            protected String text = "";

            public List<String> getItems() {
                return this.item;
            }

            public String getText() {
                return text;
            }

            public void setText(String value) {
                this.text = value;
            }
        }
    }
}
