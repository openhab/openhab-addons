/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.dto;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * See {@link DeviceListModel}.
 *
 * @author Ulrich Mertin - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "levelcontrol")
public class LevelControlModel {

    @XmlElement(name = "level")
    private BigDecimal level;

    @XmlElement(name = "levelpercentage")
    private BigDecimal levelPercentage;

    public BigDecimal getLevel() {
        return level != null ? level : BigDecimal.ZERO;
    }

    public void setLevel(BigDecimal level) {
        this.level = level;
    }

    public BigDecimal getLevelPercentage() {
        return levelPercentage != null ? levelPercentage : BigDecimal.ZERO;
    }

    public void setLevelPercentage(BigDecimal levelPercentage) {
        this.levelPercentage = levelPercentage;
    }

    @Override
    public String toString() {
        return new StringBuilder("[level=").append(getLevel()).append(",levelpercentage=").append(getLevelPercentage())
                .append("]").toString();
    }
}
