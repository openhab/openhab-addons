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
package org.openhab.binding.bosesoundtouch.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringEscapeUtils;
import org.openhab.core.types.StateOption;

import com.google.gson.annotations.Expose;

/**
 * The {@link ContentItem} class manages a ContentItem
 *
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer - Initial contribution
 */
public class ContentItem {

    private String source;
    private String sourceAccount;
    private String location;
    private boolean presetable;
    private String itemName;
    private int presetID;
    private String containerArt;
    @Expose
    private final Map<String, String> additionalAttributes;

    /**
     * Creates a new instance of this class
     */
    public ContentItem() {
        source = "";
        sourceAccount = null;
        location = null;
        presetable = false;
        itemName = null;
        presetID = 0;
        containerArt = null;
        additionalAttributes = new HashMap<>();
    }

    /**
     * Returns true if this ContentItem is defined as Preset
     *
     * @return true if this ContentItem is defined as Preset
     */
    public boolean isPreset() {
        if (presetable) {
            return presetID > 0;
        } else {
            return false;
        }
    }

    /**
     * Returns true if all necessary stats are set
     *
     * @return true if all necessary stats are set
     */
    public boolean isValid() {
        if (getOperationMode() == OperationModeType.STANDBY) {
            return true;
        }
        if (itemName == null || source == null || itemName.isEmpty() || source.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Returns true if source, sourceAccount, location, itemName, and presetable are equal
     *
     * @return true if source, sourceAccount, location, itemName, and presetable are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ContentItem) {
            ContentItem other = (ContentItem) obj;
            if (!Objects.equals(other.source, this.source)) {
                return false;
            }
            if (!Objects.equals(other.sourceAccount, this.sourceAccount)) {
                return false;
            }
            if (other.presetable != this.presetable) {
                return false;
            }
            if (!Objects.equals(other.location, this.location)) {
                return false;
            }
            if (!Objects.equals(other.itemName, this.itemName)) {
                return false;
            }
            return true;
        }
        return super.equals(obj);
    }

    /**
     * Returns the operation Mode, depending on the stats that are set
     *
     * @return the operation Mode, depending on the stats that are set
     */
    public OperationModeType getOperationMode() {
        OperationModeType operationMode = OperationModeType.OTHER;
        if (source == null || source.equals("")) {
            return OperationModeType.OTHER;
        }
        if (source.contains("PRODUCT")) {
            if (sourceAccount.contains("TV")) {
                operationMode = OperationModeType.TV;
            }
            if (sourceAccount.contains("HDMI")) {
                operationMode = OperationModeType.HDMI1;
            }
            return operationMode;
        }
        try {
            operationMode = OperationModeType.valueOf(source);
            return operationMode;
        } catch (IllegalArgumentException iae) {
            return OperationModeType.OTHER;
        }
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setSourceAccount(String sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setAdditionalAttribute(String name, String value) {
        this.additionalAttributes.put(name, value);
    }

    public void setPresetable(boolean presetable) {
        this.presetable = presetable;
    }

    public void setPresetID(int presetID) {
        this.presetID = presetID;
    }

    public void setContainerArt(String containerArt) {
        this.containerArt = containerArt;
    }

    public String getSource() {
        return source;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public String getLocation() {
        return location;
    }

    public String getItemName() {
        return itemName;
    }

    public boolean isPresetable() {
        return presetable;
    }

    public int getPresetID() {
        return presetID;
    }

    public String getContainerArt() {
        return containerArt;
    }

    /**
     * Returns the XML Code that is needed to switch to this ContentItem
     *
     * @return the XML Code that is needed to switch to this ContentItem
     */
    public String generateXML() {
        String xml;
        switch (getOperationMode()) {
            case BLUETOOTH:
                xml = "<ContentItem source=\"BLUETOOTH\"></ContentItem>";
                break;
            case AUX:
            case AUX1:
            case AUX2:
            case AUX3:
                xml = "<ContentItem source=\"AUX\" sourceAccount=\"" + sourceAccount + "\"></ContentItem>";
                break;
            case TV:
                xml = "<ContentItem source=\"PRODUCT\" sourceAccount=\"TV\" isPresetable=\"false\" />";
                break;
            case HDMI1:
                xml = "<ContentItem source=\"PRODUCT\" sourceAccount=\"HDMI_1\" isPresetable=\"false\" />";
                break;
            default:
                StringBuilder sbXml = new StringBuilder("<ContentItem");
                if (source != null) {
                    sbXml.append(" source=\"").append(StringEscapeUtils.escapeXml(source)).append("\"");
                }
                if (location != null) {
                    sbXml.append(" location=\"").append(StringEscapeUtils.escapeXml(location)).append("\"");
                }
                if (sourceAccount != null) {
                    sbXml.append(" sourceAccount=\"").append(StringEscapeUtils.escapeXml(sourceAccount)).append("\"");
                }
                sbXml.append(" isPresetable=\"").append(presetable).append("\"");
                for (Map.Entry<String, String> aae : additionalAttributes.entrySet()) {
                    sbXml.append(" ").append(aae.getKey()).append("=\"")
                            .append(StringEscapeUtils.escapeXml(aae.getValue())).append("\"");
                }
                sbXml.append(">");
                if (itemName != null) {
                    sbXml.append("<itemName>").append(itemName).append("</itemName>");
                }
                if (containerArt != null) {
                    sbXml.append("<containerArt>").append(containerArt).append("</containerArt>");
                }
                sbXml.append("</ContentItem>");
                xml = sbXml.toString();
                break;
        }
        return xml;
    }

    public StateOption toStateOption() {
        String stateOptionLabel = String.valueOf(presetID) + ": " + itemName;
        return new StateOption(String.valueOf(presetID), stateOptionLabel);
    }

    @Override
    public String toString() {
        // if (presetID >= 1 && presetID <= 6) {
        // StringBuilder buffer = new StringBuilder();
        // buffer.append("PRESET_");
        // buffer.append(presetID);
        // return buffer.toString();
        // }
        return itemName;
    }
}
