/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.ircc.models;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * This class represents the status element in the XML for a IRCC device. The XML that will be deserialized will look
 * like
 *
 * <pre>
 * {@code
    <status name="viewing">
        <statusItem field="source" value="Net" />
        <statusItem field="title" value="Shameless Series 4 Episode 3" />
        <statusItem field="serviceId" value="2099" />
        <statusItem field="assetId" value="29988" />
        <statusItem field="provider" value="ABC_iView" />
    </status>
 * }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@XStreamAlias("status")
public class IrccStatus {

    /** The constant where the status is on a text input */
    public static final String TEXTINPUT = "textInput";

    /** The constant where the status is on a disc */
    public static final String DISC = "disc";

    /** The constant where the status is on a browser */
    public static final String WEBBROWSER = "webBrowse";

    /** The constant where the status is a cursor */
    public static final String CURSORDISPLAY = "cursorDisplay";

    /** The constant where the status is viewing something */
    public static final String VIEWING = "viewing";

    /** The name for the status (one of the constants above) */
    @XStreamAsAttribute
    @XStreamAlias("name")
    private @Nullable String name;

    /** The various items making up the status */
    @Nullable
    @XStreamImplicit
    private List<@Nullable IrccStatusItem> items;

    /**
     * Gets the status name
     *
     * @return the possibly null, possibly empty status name
     */
    public @Nullable String getName() {
        return name;
    }

    /**
     * Gets the the particular status item value
     *
     * @param name the non-null, non-empty item field name
     * @return the possibly null value for the field
     */
    public @Nullable String getItemValue(final String name) {
        Validate.notEmpty(name, "name cannot be empty");

        final List<@Nullable IrccStatusItem> localItems = items;
        if (localItems != null) {
            for (final IrccStatusItem item : localItems) {
                if (item != null && StringUtils.equalsIgnoreCase(name, item.getField())) {
                    return item.getValue();
                }
            }
        }
        return null;
    }
}
