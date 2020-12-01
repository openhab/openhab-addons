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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * This class represents the deserialized results of an UPNP service XML. The following is an example of the
 * results that will be deserialized:
 *
 * <pre>
 * {@code
      <?xml version="1.0"?>
      <scpd xmlns="urn:schemas-upnp-org:service-1-0">
        <specVersion>
          <major>1</major>
          <minor>0</minor>
        </specVersion>
        <actionList>
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
        <action>
          <name>X_GetStatus</name>
          <argumentList>
            <argument>
              <name>CategoryCode</name>
              <direction>in</direction>
              <relatedStateVariable>X_A_ARG_TYPE_Category</relatedStateVariable>
            </argument>
            <argument>
              <name>CurrentStatus</name>
              <direction>out</direction>
              <relatedStateVariable>X_A_ARG_TYPE_CurrentStatus</relatedStateVariable>
            </argument>
            <argument>
              <name>CurrentCommandInfo</name>
              <direction>out</direction>
              <relatedStateVariable>X_A_ARG_TYPE_CurrentCommandInfo</relatedStateVariable>
            </argument>
          </argumentList>
        </action>
      </actionList>
      <serviceStateTable>
        <stateVariable sendEvents="no">
          <name>X_A_ARG_TYPE_IRCCCode</name>
          <dataType>string</dataType>
        </stateVariable>
        <stateVariable sendEvents="no">
          <name>X_A_ARG_TYPE_Category</name>
          <dataType>string</dataType>
        </stateVariable>
        <stateVariable sendEvents="no">
          <name>X_A_ARG_TYPE_CurrentStatus</name>
          <dataType>string</dataType>
          <allowedValueList>
            <allowedValue>0</allowedValue>
            <allowedValue>801</allowedValue>
            <allowedValue>804</allowedValue>
            <allowedValue>805</allowedValue>
            <allowedValue>806</allowedValue>
          </allowedValueList>
        </stateVariable>
        <stateVariable sendEvents="no">
          <name>X_A_ARG_TYPE_CurrentCommandInfo</name>
          <dataType>string</dataType>
        </stateVariable>
      </serviceStateTable>
    </scpd>
 * }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@XStreamAlias("scpd")
public class UpnpScpd {
    @XStreamAlias("serviceStateTable")
    private @Nullable UpnpScpdStateTable stateTable;

    @XStreamAlias("actionList")
    private @Nullable UpnpScpdActionList actionList;

    /**
     * Gets the action for the given action name
     *
     * @param actionName a non-null, non-empty action name
     * @return a possibly null action (null if not found)
     */
    public @Nullable UpnpScpdAction getAction(final String actionName) {
        final UpnpScpdActionList localActionList = actionList;
        final List<UpnpScpdAction> actions = localActionList == null ? null : localActionList.actions;
        if (actions != null) {
            for (final UpnpScpdAction action : actions) {
                if (StringUtils.equalsIgnoreCase(actionName, action.getActionName())) {
                    return action;
                }
            }
        }
        return null;
    }

    /**
     * Gets the SOAP request for the specified serviceType, action name and parameters
     *
     * @param serviceType a non-null, non-empty service type
     * @param actionName a non-null, non-empty action name
     * @param parms the optional parameters
     * @return a string representing the SOAP action or null if serviceType/actionName was not found
     */
    public @Nullable String getSoap(final String serviceType, final String actionName, final String... parms) {
        Validate.notEmpty(serviceType, "serviceType cannnot be empty");
        Validate.notEmpty(actionName, "actionName cannnot be empty");

        final UpnpScpdActionList localActionList = actionList;
        final List<UpnpScpdAction> actions = localActionList == null ? null : localActionList.actions;
        if (actions != null) {
            for (final UpnpScpdAction action : actions) {
                if (StringUtils.equalsIgnoreCase(actionName, action.getActionName())) {
                    final StringBuilder sb = new StringBuilder(
                            "<?xml version=\"1.0\"?>\n<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n  <s:Body>\n    <u:");
                    sb.append(actionName);
                    sb.append(" xmlns:u=\"");
                    sb.append(serviceType);
                    sb.append("\">");

                    int parmIdx = 0;
                    for (final UpnpScpdArgument arg : action.getArguments()) {
                        if (arg.isIn()) {
                            final String parm = parmIdx >= parms.length ? "" : parms[parmIdx++];
                            sb.append("\n<");
                            sb.append(arg.getArgName());
                            sb.append(">");
                            sb.append(parm);
                            sb.append("</");
                            sb.append(arg.getArgName());
                            sb.append(">");
                        }
                    }

                    sb.append("\n    </u:");
                    sb.append(actionName);
                    sb.append(">\n  </s:Body>\n</s:Envelope>\n");
                    return sb.toString();
                }
            }
        }
        return null;
    }

    /**
     * The action list class that holds the list of actions
     *
     * @author Tim Roberts - Initial Contribution
     *
     */
    @NonNullByDefault
    class UpnpScpdActionList {
        @XStreamImplicit
        private @Nullable List<UpnpScpdAction> actions;
    }

    /**
     * The state table that holds a list of state variables
     *
     * @author Tim Roberts - Initial Contribution
     *
     */
    @NonNullByDefault
    public class UpnpScpdStateTable {
        @XStreamImplicit
        private @Nullable List<UpnpScpdStateVariable> variables;
    }
}
