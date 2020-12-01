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
package org.openhab.binding.sony.internal.dial.models;

import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class represents the DIAL application state. The state will be retrieved from a call to {@link #get(URL)} and
 * the XML looks like the following
 *
 * <pre>
 * {@code
   <?xml version="1.0" encoding="UTF-8"?>
   <service xmlns="urn:dial-multiscreen-org:schemas:dial">
      <name>com.sony.videoexplorer</name>
      <options allowStop="true" wolMac="30:52:cb:8f:b3:77"/>
      <state>stopped</state>
   </service>
 * }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */
@XStreamAlias("service")
@NonNullByDefault
public class DialAppState {

    /**
     * The state for running. Also valid are "stopped" and "installUrl=url"
     */
    private static final String RUNNING = "running";

    /**
     * The application state. Please note that the application state has been broken by sony for quite awhile
     * (everything says stopped)
     */
    private @Nullable String state;

    /**
     * Checks if is running.
     *
     * @return true, if is running
     */
    public boolean isRunning() {
        return StringUtils.equalsIgnoreCase(RUNNING, state);
    }

    /**
     * Get's the DIAL application state from the given content
     *
     * @param xml the non-null, non-empty XML
     * @return a {@link DialAppState} or null if cannot be parsed
     */
    public static @Nullable DialAppState get(final String xml) {
        Validate.notEmpty(xml, "xml cannot be empty");
        return DialXmlReader.APPSTATE.fromXML(xml);
    }
}
