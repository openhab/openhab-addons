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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * This class represents the deserialized results of an IRCC system information query. The following is an example of
 * the results that will be deserialized:
 *
 * <pre>
 * {@code
    <?xml version="1.0" encoding="UTF-8"?>
    <systemInformation>
        <name>BDPlayer</name>
        <generation>2017</generation>
        <remoteType>RMT-B119A</remoteType>
        <remoteType>RMT-B120A</remoteType>
        <remoteType>RMT-B122A</remoteType>
        <remoteType>RMT-B123A</remoteType>
        <remoteType bundled="true">RMT-B126A</remoteType>
        <remoteType>RMT-B119J</remoteType>
        <remoteType>RMT-B127J</remoteType>
        <remoteType>RMT-B119P</remoteType>
        <remoteType>RMT-B120P</remoteType>
        <remoteType>RMT-B121P</remoteType>
        <remoteType>RMT-B122P</remoteType>
        <remoteType>RMT-B127P</remoteType>
        <remoteType>RMT-B119C</remoteType>
        <remoteType>RMT-B120C</remoteType>
        <remoteType>RMT-B122C</remoteType>
        <remoteType>RMT-B127C</remoteType>
        <remoteType>RMT-B127T</remoteType>
        <remoteType>RMT-B115A</remoteType>
        <actionHeader name="CERS-DEVICE-ID"/>
        <supportContentsClass>
            <class>video</class>
            <class>music</class>
        </supportContentsClass>
        <supportSource>
            <source>BD</source>
            <source>DVD</source>
            <source>CD</source>
            <source>Net</source>
        </supportSource>
        <supportFunction>
            <function name="Notification"/>
            <function name="WOL">
                <functionItem field="MAC" value="04-5d-4b-24-d9-ff"/>
            </function>
        </supportFunction>
    </systemInformation>
 * }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@XStreamAlias("systemInformation")
public class IrccSystemInformation {

    /** The action header for the system information */
    @XStreamAlias("actionHeader")
    @XStreamAsAttribute
    private @Nullable String actionHeader;

    /** The supported functions */
    @XStreamAlias("supportFunction")
    private @Nullable SupportedFunction supportedFunction;

    /**
     * Gets the action header or returns "CERS-DEVICE-ID" if none found
     *
     * @return the action header or "CERS-DEVICE-ID" if none found
     */
    public String getActionHeader() {
        final String ah = actionHeader;
        return StringUtils.defaultIfEmpty(ah, "CERS-DEVICE-ID");
    }

    /**
     * Gets the WOL mac address
     *
     * @return a possibly null (if not found), never empty MAC address
     */
    public @Nullable String getWolMacAddress() {
        final SupportedFunction sf = supportedFunction;
        if (sf == null) {
            return null;
        }

        final List<Function> funcs = sf.functions;
        if (funcs != null) {
            for (final Function func : funcs) {
                if (func != null && StringUtils.equalsIgnoreCase("wol", func.name)) {
                    final List<@Nullable FunctionItem> localItems = func.items;
                    if (localItems != null) {
                        for (final FunctionItem fi : localItems) {
                            if (fi != null && StringUtils.equalsIgnoreCase("mac", fi.field)
                                    && StringUtils.isNotEmpty(fi.value)) {
                                return fi.value;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * The supported function class that contains a list of functions
     *
     * @author Tim Roberts - Initial Contribution
     */
    @NonNullByDefault
    class SupportedFunction {
        @XStreamImplicit
        private @Nullable List<Function> functions;
    }

    /**
     * The function class that provides the name of the function and the list of items the function supports
     *
     * @author Tim Roberts - Initial Contribution
     */
    @NonNullByDefault
    @XStreamAlias("function")
    class Function {
        @XStreamAlias("name")
        @XStreamAsAttribute
        private @Nullable String name;

        @XStreamImplicit
        private @Nullable List<@Nullable FunctionItem> items;
    }

    /**
     * The function item class that describes the item by it's field and value
     *
     * @author Tim Roberts - Initial Contribution
     */
    @NonNullByDefault
    @XStreamAlias("functionItem")
    class FunctionItem {
        @XStreamAlias("field")
        @XStreamAsAttribute
        private @Nullable String field;

        @XStreamAlias("value")
        @XStreamAsAttribute
        private @Nullable String value;
    }
}
