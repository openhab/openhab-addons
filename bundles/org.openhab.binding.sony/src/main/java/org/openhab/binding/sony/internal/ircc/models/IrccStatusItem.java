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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class represents the status element item in the XML for a IRCC device. The XML that will be deserialized will
 * look like:
 *
 * <pre>
 * {@code
    <statusItem field="source" value="Net" />
 * }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@XStreamAlias("statusItem")
public class IrccStatusItem {

    /** The constant for the class field (only valid for a status of {@link IrccStatus#VIEWING}) */
    public static final String CLASS = "class";

    /** The constant for the identifier field (only valid for a status of {@link IrccStatus#VIEWING}) */
    public static final String ID = "id";

    /** The constant for the title field (only valid for a status of {@link IrccStatus#VIEWING}) */
    public static final String TITLE = "title";

    /** The constant for the source field (only valid for a status of {@link IrccStatus#VIEWING}) */
    public static final String SOURCE = "source";

    /** The constant for the source2 field (only valid for a status of {@link IrccStatus#VIEWING}) */
    public static final String SOURCE2 = "zone2Source";

    /** The constant for the duration field (only valid for a status of {@link IrccStatus#VIEWING}) */
    public static final String DURATION = "duration";

    // fyi - DISC has "type", "mediatype", "mediaformat" - not used

    /** The field name identifing the status item */
    @XStreamAsAttribute
    @XStreamAlias("field")
    private @Nullable String field;

    /** The value of the field */
    @XStreamAsAttribute
    @XStreamAlias("value")
    private @Nullable String value;

    /**
     * Gets the field name
     *
     * @return the possibly null, possibly empty field name
     */
    public @Nullable String getField() {
        return field;
    }

    /**
     * Gets the field value
     *
     * @return the possibly null, possibly empty field name
     */
    public @Nullable String getValue() {
        return value;
    }
}
