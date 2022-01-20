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
package org.openhab.binding.lutron.internal.protocol.leap.dto;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.openhab.binding.lutron.internal.protocol.leap.AbstractMessageBody;

import com.google.gson.annotations.SerializedName;

/**
 * LEAP ButtonGroup Object
 *
 * @author Bob Adair - Initial contribution
 */
public class ButtonGroup extends AbstractMessageBody {
    public static final Pattern BUTTONGROUP_HREF_PATTERN = Pattern.compile("/buttongroup/([0-9]+)");
    public static final Pattern BUTTON_HREF_PATTERN = Pattern.compile("/button/([0-9]+)");

    @SerializedName("href")
    public String href;
    @SerializedName("Parent") // device href
    public Href parent = new Href();
    @SerializedName("Buttons")
    public Href[] buttons;
    @SerializedName("AffectedZones")
    public AffectedZone[] affectedZones;
    @SerializedName("SortOrder")
    public Integer sortOrder;
    @SerializedName("StopIfMoving")
    public String stopIfMoving; // Enabled or Disabled
    @SerializedName("ProgrammingType")
    public String programmingType; // Column

    public ButtonGroup() {
    }

    public int getButtonGroup() {
        return hrefNumber(BUTTONGROUP_HREF_PATTERN, href);
    }

    public int getParentDevice() {
        if (parent != null && parent.href != null) {
            return hrefNumber(Device.DEVICE_HREF_PATTERN, parent.href);
        } else {
            return 0;
        }
    }

    public List<Integer> getButtonList() {
        LinkedList<Integer> buttonNumList = new LinkedList<>();
        for (Href button : buttons) {
            int buttonNum = hrefNumber(BUTTON_HREF_PATTERN, button.href);
            buttonNumList.add(buttonNum);
        }
        return buttonNumList;
    }
}
