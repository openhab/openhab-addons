/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal.items;

import org.openhab.binding.bosesoundtouch.internal.XMLResponseHandler;
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
    private final Logger logger = LoggerFactory.getLogger(XMLResponseHandler.class);

    private OperationModeType operationMode;
    private String source;
    private String sourceAccount;
    private String location;
    private boolean presetable;
    private String itemName;
    private int unusedField;

    private int presetID;

    public ContentItem() {
        itemName = "";
        presetID = 0;
    }

    public boolean isPreset() {
        if (presetable) {
            return presetID > 0;
        } else {
            return false;
        }
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ContentItem) {
            ContentItem other = (ContentItem) obj;
            if (other.operationMode != this.operationMode) {
                return false;
            }
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

    public OperationModeType getOperationMode() {
        if (source == null || source.equals("")) {
            return OperationModeType.OTHER;
        }
        try {
            operationMode = OperationModeType.valueOf(source);
        } catch (IllegalArgumentException iae) {

            // logger.error("{}: Unknown SourceType: '{}' - needs to be defined!", handler.getDeviceName(), source);
            logger.error("Unknown SourceType: '{}' - needs to be defined!", source);
        }
        if ((operationMode == OperationModeType.OTHER) && (source.contains("PRODUCT"))) {
            if (sourceAccount.contains("TV")) {
                operationMode = OperationModeType.TV;
            }
            if (sourceAccount.contains("HDMI")) {
                operationMode = OperationModeType.HDMI;
            }
        }
        return operationMode;
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

    public String generateXML() {
        String xml;
        switch (getOperationMode()) {
            case BLUETOOTH:
                xml = "<ContentItem source=\"BLUETOOTH\"></ContentItem>";
                break;
            case AUX:
                xml = "<ContentItem source=\"AUX\" sourceAccount=\"AUX\"></ContentItem>";
                break;
            case TV:
                xml = "<ContentItem unusedField=\"0\" source=\"PRODUCT\" sourceAccount=\"TV\" isPresetable=\"false\" />";
                break;
            case HDMI:
                xml = "<ContentItem unusedField=\"0\" source=\"PRODUCT\" sourceAccount=\"HDMI_1\" isPresetable=\"false\" />";
                break;
            case INTERNET_RADIO:
            case STORED_MUSIC:
            default:
                xml = "<ContentItem " + "unusedField=" + "\"" + unusedField + "\" " + "source=" + "\"" + source + "\" "
                        + "location=" + "\"" + location + "\" " + "sourceAccount=" + "\"" + sourceAccount + "\""
                        + "isPresetable=" + "\"" + presetable + "\">" + "<itemName>" + itemName
                        + "</itemName></ContentItem>";
                break;
        }
        return xml;
    }

    @Override
    public String toString() {
        if (presetID >= 1 && presetID <= 6) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("PRESET_");
            buffer.append(presetID);
            return buffer.toString();
        }
        return itemName;
    }
}
