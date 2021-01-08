package org.openhab.binding.avmfritz.internal.dto;

import javax.xml.bind.annotation.*;

/**
 * See {@link DeviceListModel}.
 *
 * @author Joshua Bacher - Initial contribution
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement(name = "colorcontrol")
public class ColorControlModel {

    @XmlAttribute(name="supported_modes")
    Integer supportedModes;

    @XmlAttribute(name="current_mode")
    Integer currentMode;


    public Integer getSupportedModes() {
        return supportedModes;
    }

    public Integer getCurrentMode() {
        return currentMode;
    }
}
