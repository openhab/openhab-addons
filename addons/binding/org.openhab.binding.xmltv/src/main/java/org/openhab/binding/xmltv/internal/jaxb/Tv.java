/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.xmltv.internal.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Java class for a TV XML root element
 *
 * @author Gaël L'hopital - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement(name = "tv")
@NonNullByDefault
public class Tv {
    @XmlElement(name = "channel")
    protected List<MediaChannel> channels = new ArrayList<>();

    @XmlElement(name = "programme")
    protected List<Programme> programmes = new ArrayList<>();

    public List<MediaChannel> getMediaChannels() {
        return this.channels;
    }

    public List<Programme> getProgrammes() {
        return this.programmes;
    }
}
