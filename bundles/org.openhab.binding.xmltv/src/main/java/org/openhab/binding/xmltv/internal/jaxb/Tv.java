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
