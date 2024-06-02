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
package org.openhab.persistence.jpa.internal.model;

import java.text.DateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * This is the DAO object used for storing and retrieving to and from database.
 *
 * @author Manfred Bergmann - Initial contribution
 *
 */

@Entity
@Table(name = "HISTORIC_ITEM")
@NonNullByDefault
public class JpaPersistentItem implements HistoricItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private @NonNullByDefault({}) Long id;

    private String name = "";
    private String realName = "";
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp = new Date();
    @Column(length = 32672) // 32k, max varchar for apache derby
    private String value = "";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    @Override
    public ZonedDateTime getTimestamp() {
        return ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public State getState() {
        return UnDefType.NULL;
    }

    @Override
    public String toString() {
        return DateFormat.getDateTimeInstance().format(getTimestamp()) + ": " + getName() + " -> " + value;
    }
}
