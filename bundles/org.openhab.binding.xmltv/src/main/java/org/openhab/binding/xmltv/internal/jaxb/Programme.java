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
package org.openhab.binding.xmltv.internal.jaxb;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Java class for a programme XML element
 *
 * @author Gaël L'hopital - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@NonNullByDefault
public class Programme {
    private static final DateTimeFormatter XMLTV_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z");

    @XmlElement(name = "title", required = true)
    protected List<WithLangType> titles = new ArrayList<>();

    @XmlElement(name = "category")
    protected List<WithLangType> categories = new ArrayList<>();

    @XmlElement(name = "icon")
    protected List<Icon> icons = new ArrayList<>();

    @XmlAttribute(required = true)
    private String start = "";

    @XmlAttribute
    private String stop = "";

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String channel = "";

    public List<WithLangType> getTitles() {
        return titles;
    }

    public List<WithLangType> getCategories() {
        return categories;
    }

    public Instant getProgrammeStart() {
        long epoch = iso860DateToEpoch(start);
        return Instant.ofEpochMilli(epoch);
    }

    public Instant getProgrammeStop() {
        long epoch = iso860DateToEpoch(stop);
        return Instant.ofEpochMilli(epoch);
    }

    private long iso860DateToEpoch(String date) {
        long epoch = ZonedDateTime.parse(date, XMLTV_DATE_FORMAT).toInstant().toEpochMilli();
        return epoch;
    }

    public List<Icon> getIcons() {
        return icons;
    }

    public String getChannel() {
        return channel;
    }
}
