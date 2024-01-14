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
package org.openhab.binding.bosesoundtouch.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.StateOption;

import com.google.gson.annotations.Expose;

/**
 * The {@link ContentItem} class manages a ContentItem
 *
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer - Initial contribution
 */
@NonNullByDefault
public class ContentItem {

    private String source = "";
    private @Nullable String sourceAccount;
    private @Nullable String location;
    private boolean presetable = false;
    private @Nullable String itemName;
    private int presetID = 0;
    private @Nullable String containerArt;
    @Expose
    private final Map<String, String> additionalAttributes = new HashMap<>();

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
        } else {
            String localItemName = itemName;
            if (localItemName != null) {
                return !(localItemName.isEmpty() || source.isEmpty());
            } else {
                return false;
            }
        }
    }

    /**
     * Returns true if source, sourceAccount, location, itemName, and presetable are equal
     *
     * @return true if source, sourceAccount, location, itemName, and presetable are equal
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof ContentItem other) {
            return Objects.equals(other.source, this.source) || Objects.equals(other.sourceAccount, this.sourceAccount)
                    || other.presetable == this.presetable || Objects.equals(other.location, this.location)
                    || Objects.equals(other.itemName, this.itemName);
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
        if ("".equals(source)) {
            return OperationModeType.OTHER;
        }
        if (source.contains("PRODUCT")) {
            String localSourceAccount = sourceAccount;
            if (localSourceAccount != null) {
                if (localSourceAccount.contains("TV")) {
                    operationMode = OperationModeType.TV;
                }
                if (localSourceAccount.contains("HDMI")) {
                    operationMode = OperationModeType.HDMI1;
                }
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

    public @Nullable String getSourceAccount() {
        return sourceAccount;
    }

    public @Nullable String getLocation() {
        return location;
    }

    public @Nullable String getItemName() {
        return itemName;
    }

    public boolean isPresetable() {
        return presetable;
    }

    public int getPresetID() {
        return presetID;
    }

    public @Nullable String getContainerArt() {
        return containerArt;
    }

    /**
     * Simple method to escape XML special characters in String.
     * There are five XML Special characters which needs to be escaped :
     * & - &amp;
     * < - &lt;
     * > - &gt;
     * " - &quot;
     * ' - &apos;
     */
    private String escapeXml(String xml) {
        xml = xml.replace("&", "&amp;");
        xml = xml.replace("<", "&lt;");
        xml = xml.replace(">", "&gt;");
        xml = xml.replace("\"", "&quot;");
        xml = xml.replace("'", "&apos;");
        return xml;
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

                sbXml.append(" source=\"").append(escapeXml(source)).append("\"");

                String localLocation = location;
                if (localLocation != null) {
                    sbXml.append(" location=\"").append(escapeXml(localLocation)).append("\"");
                }
                String localSourceAccount = sourceAccount;
                if (localSourceAccount != null) {
                    sbXml.append(" sourceAccount=\"").append(escapeXml(localSourceAccount)).append("\"");
                }
                sbXml.append(" isPresetable=\"").append(presetable).append("\"");
                for (Map.Entry<String, String> aae : additionalAttributes.entrySet()) {
                    sbXml.append(" ").append(aae.getKey()).append("=\"").append(escapeXml(aae.getValue())).append("\"");
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
        String stateOptionLabel = presetID + ": " + itemName;
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
        String localString = itemName;
        return (localString != null) ? localString : "";
    }
}
