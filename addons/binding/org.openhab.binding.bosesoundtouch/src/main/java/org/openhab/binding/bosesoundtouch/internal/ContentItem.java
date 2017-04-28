/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal;

import org.openhab.binding.bosesoundtouch.types.OperationModeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ContentItem} class manages a ContentItem
 *
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer
 */
public class ContentItem {
    private final Logger logger = LoggerFactory.getLogger(ContentItem.class);

    private String source;
    private String sourceAccount;
    private String location;
    private boolean presetable;
    private String itemName;
    private int unusedField;
    private int presetID;

    /**
     * Creates a new instance of this class
     */
    public ContentItem() {
        source = "";
        sourceAccount = "";
        location = "";
        presetable = false;
        itemName = "";
        unusedField = 0;
        presetID = 0;
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
        if (itemName.equals("") || source.equals("")) {
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
            if (!isEqual(other.source, this.source)) {
                return false;
            }
            if (!isEqual(other.sourceAccount, this.sourceAccount)) {
                return false;
            }
            if (other.presetable != this.presetable) {
                return false;
            }
            if (!isEqual(other.location, this.location)) {
                return false;
            }
            if (!isEqual(other.itemName, this.itemName)) {
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
            logger.warn("Unknown SourceType: '{}' - needs to be defined!", source);
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

    public void setUnusedField(int unusedField) {
        this.unusedField = unusedField;
    }

    public void setPresetable(boolean presetable) {
        this.presetable = presetable;
    }

    public void setPresetID(int presetID) {
        this.presetID = presetID;
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

    public int getUnusedField() {
        return unusedField;
    }

    public boolean isPresetable() {
        return presetable;
    }

    public int getPresetID() {
        return presetID;
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
                xml = "<ContentItem unusedField=\"0\" source=\"PRODUCT\" sourceAccount=\"TV\" isPresetable=\"false\" />";
                break;
            case HDMI1:
                xml = "<ContentItem unusedField=\"0\" source=\"PRODUCT\" sourceAccount=\"HDMI_1\" isPresetable=\"false\" />";
                break;
            default:
                xml = "<ContentItem " + "unusedField=" + "\"" + unusedField + "\" " + "source=" + "\"" + source + "\" "
                        + "location=" + "\"" + location + "\" " + "sourceAccount=" + "\"" + sourceAccount + "\" "
                        + "isPresetable=" + "\"" + presetable + "\">" + "<itemName>" + itemName
                        + "</itemName></ContentItem>";
                break;
        }
        return xml;
    }

    @Override
    public String toString() {
        // if (presetID >= 1 && presetID <= 6) {
        // StringBuffer buffer = new StringBuffer();
        // buffer.append("PRESET_");
        // buffer.append(presetID);
        // return buffer.toString();
        // }
        return itemName;
    }

    /**
     * Returns a string, to save the contentItem
     *
     * @return a string, to save the contentItem
     */
    public String stringToSave() {
        // private int presetID;
        // private String source;
        // private String sourceAccount;
        // private String location;
        // private String itemName;
        // private int unusedField;

        StringBuffer sb = new StringBuffer();
        sb.append(presetID);
        sb.append(";");
        sb.append(source);
        sb.append(";");
        sb.append(sourceAccount);
        sb.append(";");
        sb.append(location);
        sb.append(";");
        sb.append(itemName);
        sb.append(";");
        sb.append(unusedField);
        sb.append(";");
        return sb.toString();
    }

    /**
     * Inits the stats with values parsed form the string s
     *
     * @param s a string written with stringToSave()
     */
    public void createFormString(String s) {
        // private int presetID;
        // private String source;
        // private String sourceAccount;
        // private String location;
        // private String itemName;
        // private int unusedField;

        String[] parts = s.split(";");

        presetID = Integer.parseInt(parts[0]);
        source = parts[1];
        sourceAccount = parts[2];
        location = parts[3];
        itemName = parts[4];
        unusedField = Integer.parseInt(parts[5]);
        presetable = true;
    }

    private boolean isEqual(String s1, String s2) {
        if (s1 == s2) {
            return true;
        }
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.equals(s2);
    }
}
