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
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * This class represents the deserialized results of an UPNP service action argument. The following is an example of the
 * results that will be deserialized:
 *
 * <pre>
 * {@code
          <action>
            <name>X_SendIRCC</name>
            <argumentList>
              <argument>
                <name>IRCCCode</name>
                <direction>in</direction>
                <relatedStateVariable>X_A_ARG_TYPE_IRCCCode</relatedStateVariable>
            </argument>
          </argumentList>
        </action>
 * }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@XStreamAlias("argument")
public class UpnpScpdArgument {

    /** The argument name */
    @XStreamAlias("name")
    private @Nullable String argName;

    /** Whether the argument is inbound or outbound */
    @XStreamAlias("direction")
    @XStreamConverter(value = BooleanConverter.class, booleans = { false }, strings = { "in", "out" })
    private boolean in;

    /** The related state variable (found in the state table) */
    @XStreamAlias("relatedStateVariable")
    private @Nullable String relatedStateVariable;

    /**
     * Gets the argument name
     *
     * @return the possibly null, possibly empty argument name
     */
    public @Nullable String getArgName() {
        return argName;
    }

    /**
     * Checks if the argument is inbound or outbound
     *
     * @return true for inbound, false otherwise
     */
    public boolean isIn() {
        return in;
    }
}
