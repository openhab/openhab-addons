/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.xmltv.internal.jaxb;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.jdt.annotation.NonNull;

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

    public @NonNull String getSrc() {
        return src;
    }

    public BigInteger getWidth() {
        return width;
    }

    public BigInteger getHeight() {
        return height;
    }
}
