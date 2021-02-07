/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.avmfritz.internal.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * See {@link DeviceListModel}.
 *
 * @author Joshua Bacher - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"level", "levelpercentage"})
@XmlRootElement(name = "levelcontrol")
public class LevelControlModel {

    // <level> Level/Niveau von 0(0%) bis 255(100%)
    public int level;
    // Level/Niveau in Prozent, 0 bis 100 Prozent
    public int levelpercentage;

    public int getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public int getLevelpercentage() {
        return levelpercentage;
    }

    public void setLevelpercentage(Integer levelpercentage) {
        this.levelpercentage = levelpercentage;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[");
        sb.append("level=").append(level);
        sb.append(", levelpercentage=").append(levelpercentage);
        sb.append(']');
        return sb.toString();
    }
}
