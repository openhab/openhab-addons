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
package org.openhab.binding.lutron.internal.protocol.leap.dto;

import java.util.regex.Pattern;

import org.openhab.binding.lutron.internal.protocol.leap.AbstractMessageBody;

import com.google.gson.annotations.SerializedName;

/**
 * LEAP ButtonStatus Object
 *
 * @author Cody Cutrer - Initial contribution
 */
public class ButtonStatus extends AbstractMessageBody {
    public static final Pattern BUTTON_HREF_PATTERN = Pattern.compile("/button/([0-9]+)");

    @SerializedName("ButtonEvent")
    public ButtonEvent buttonEvent;
    @SerializedName("Button")
    public Href button = new Href();

    public ButtonStatus() {
    }

    public int getButton() {
        if (button != null) {
            return hrefNumber(BUTTON_HREF_PATTERN, button.href);
        } else {
            return 0;
        }
    }
}
