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

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 *
 * This class represents the deserialized results of an UPNP service action. The following is an example of the
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
@XStreamAlias("action")
public class UpnpScpdAction {

    /** The action name */
    @XStreamAlias("name")
    private @Nullable String actionName;

    /** The arguments */
    @XStreamAlias("argumentList")
    private @Nullable UpnpScpdArgumentList argumentList;

    /**
     * Gets the action name.
     *
     * @return the action name
     */
    public @Nullable String getActionName() {
        return actionName;
    }

    /**
     * Returns the list of arguments for this action
     *
     * @return a possibly empty list of arguments
     */
    public List<UpnpScpdArgument> getArguments() {
        final UpnpScpdArgumentList localArgumentList = argumentList;
        final List<UpnpScpdArgument> arguments = localArgumentList == null ? null : localArgumentList.arguments;
        return arguments == null ? Collections.emptyList() : Collections.unmodifiableList(arguments);
    }

    /**
     * The list of arguments for the action
     *
     * @author Tim Roberts - Initial Contribution
     *
     */
    @NonNullByDefault
    class UpnpScpdArgumentList {
        @XStreamImplicit
        private @Nullable List<UpnpScpdArgument> arguments;
    }
}
