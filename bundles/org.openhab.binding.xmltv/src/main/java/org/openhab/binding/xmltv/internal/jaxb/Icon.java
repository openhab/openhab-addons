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
package org.openhab.binding.xmltv.internal.jaxb;

import java.math.BigInteger;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Java class for an icon XML element
 *
 * @author Gaël L'hopital - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement(name = "icon")
public class Icon {

    @XmlAttribute(required = true)
    protected String src;

    @XmlAttribute
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger width;

    @XmlAttribute
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger height;

    public String getSrc() {
        return src;
    }

    public BigInteger getWidth() {
        return width;
    }

    public BigInteger getHeight() {
        return height;
    }
}
