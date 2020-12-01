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
 * This class represents the deserialized results of an IRCC actionList command. The following is an example of the
 * results that will be deserialized:
 *
 * <pre>
 * {@code
        <?xml version="1.0" encoding="UTF-8"?>
        <actionList>
          <action name="register" mode="3" url="http://192.168.1.11:50002/register"/>
          <action name="getText" url="http://192.168.1.11:50002/getText"/>
          <action name="sendText" url="http://192.168.1.11:50002/sendText"/>
          <action name="getContentInformation" url="http://192.168.1.11:50002/getContentInformation"/>
          <action name="getSystemInformation" url="http://192.168.1.11:50002/getSystemInformation"/>
          <action name="getRemoteCommandList" url="http://192.168.1.11:50002/getRemoteCommandList"/>
          <action name="getStatus" url="http://192.168.1.11:50002/getStatus"/>
          <action name="getHistoryList" url="http://192.168.1.11:50002/getHistoryList"/>
          <action name="getContentUrl" url="http://192.168.1.11:50002/getContentUrl"/>
          <action name="sendContentUrl" url="http://192.168.1.11:50002/sendContentUrl"/>
        </actionList>
 * }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@XStreamAlias("actionList")
public class IrccActionList {
    /**
     * The list of actions found in the XML (can be null or empty)
     */
    @XStreamImplicit
    private @Nullable List<@Nullable IrccAction> actions;

    /**
     * Gets the url for the given action name
     *
     * @param actionName the non-null, non-empty action name
     * @return the url for action or null if not found
     */
    @Nullable
    public String getUrlForAction(final String actionName) {
        Validate.notEmpty(actionName, "actionName cannot be empty");

        final List<@Nullable IrccAction> localActions = actions;
        if (localActions != null) {
            for (final IrccAction action : localActions) {
                if (action != null && StringUtils.equalsIgnoreCase(actionName, action.getName())) {
                    return action.getUrl();
                }
            }
        }
        return null;
    }

    /**
     * Gets the registration mode for the action list or 0 if none (or invalid)
     *
     * @return the registration mode (should but not guaranteed to be >= 0)
     */
    public int getRegistrationMode() {
        final List<@Nullable IrccAction> localActions = actions;
        if (localActions != null) {
            for (final IrccAction action : localActions) {
                if (action != null && StringUtils.isNotEmpty(action.getMode())
                        && StringUtils.equalsIgnoreCase(IrccAction.REGISTER, action.getName())) {
                    try {
                        return Integer.parseInt(action.getMode());
                    } catch (final NumberFormatException e) {
                        return 0;
                    }
                }
            }
        }
        return 0;
    }

    /**
     * The following class represents the IRCC action and is internal to the IrccActionList class. However, it cannot be
     * private since the xstream reader needs access to the annotations
     *
     * @author Tim Roberts - Initial contribution
     */
    @NonNullByDefault
    @XStreamAlias("action")
    class IrccAction {
        /**
         * Represents the register action name
         */
        private static final String REGISTER = "register";

        /**
         * Represents the action name
         */
        @XStreamAlias("name")
        @XStreamAsAttribute
        private @Nullable String name;

        /**
         * Represents the URL for the action
         */
        @XStreamAlias("url")
        @XStreamAsAttribute
        private @Nullable String url;

        /**
         * Represents the IRCC registration mode and is ONLY valid if the action name is {@link #REGISTER} and should be
         * a number
         */
        @XStreamAlias("mode")
        @XStreamAsAttribute
        private @Nullable String mode;

        /**
         * Get's the name of the action
         * 
         * @return a possibly null, possibly empty name
         */
        public @Nullable String getName() {
            return name;
        }

        /**
         * Get's the registration mode
         * 
         * @return a possibly null, possibly empty registration mode
         */
        public @Nullable String getMode() {
            return mode;
        }

        /**
         * Get's the action URL
         * 
         * @return a possibly null, possibly empty action url
         */
        public @Nullable String getUrl() {
            return url;
        }
    }
}
