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
 * This class represents the deserialized results of an IRCC info item. The following is an example of the results that
 * will be deserialized.
 *
 * <pre>
 * {@code
      <infoItem field="class" value="video"/>
 * }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@XStreamAlias("infoItem")
class IrccInfoItem {
    /** The name of the info item */
    @XStreamAsAttribute
    @XStreamAlias("field")
    private @Nullable String name;

    /** The value related to the name */
    @XStreamAsAttribute
    @XStreamAlias("value")
    private @Nullable String value;

    /**
     * Gets the name
     *
     * @return the possibly null, possibly empty name
     */
    public @Nullable String getName() {
        return name;
    }

    /**
     * Gets the value
     *
     * @return the possibly null, possibly empty value
     */
    public @Nullable String getValue() {
        return value;
    }
}
