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
package org.openhab.binding.denonmarantz.internal.xml.entities.commands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Holds text values with a certain id
 *
 * @author Jeroen Idserda - Initial contribution
 */
@XmlRootElement(name = "text")
@XmlAccessorType(XmlAccessType.FIELD)
@NonNullByDefault
public class Text {

    @XmlAttribute(name = "id")
    private @Nullable String id;

    @XmlValue
    private @Nullable String value;

    public Text() {
    }

    public @Nullable String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public @Nullable String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
