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
    Integer level;
    // Level/Niveau in Prozent, 0 bis 100 Prozent
    Integer levelpercentage;

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getLevelpercentage() {
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
