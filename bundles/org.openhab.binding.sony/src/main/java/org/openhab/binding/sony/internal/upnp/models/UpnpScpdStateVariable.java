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
package org.openhab.binding.sony.internal.upnp.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class represents the deserialized results of an UPNP state variable. The following is an example of the
 * results that will be deserialized:
 *
 * <pre>
 * {@code
        <stateVariable sendEvents="no">
          <name>X_A_ARG_TYPE_IRCCCode</name>
          <dataType>string</dataType>
        </stateVariable>
 * }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@XStreamAlias("stateVariable")
public class UpnpScpdStateVariable {
    /** The state variable name */
    @XStreamAlias("name")
    private @Nullable String name;

    /** The state variable data type */
    @XStreamAlias("dataType")
    private @Nullable String dataType;

    /**
     * Returns the name of the state variable
     * 
     * @return a possibly null, possibly empty name
     */
    public @Nullable String getName() {
        return name;
    }

    /**
     * Returns the data type of the state variable
     * 
     * @return a possibly null, possibly empty data type
     */
    public @Nullable String getDataType() {
        return dataType;
    }
}
