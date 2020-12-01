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

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * This class represents a single DIAL application. The element being deserialized will typically look like:
 *
 * <pre>
 * {@code
     <app>
       <id>com.sony.videoexplorer</id>
       <name>Video Explorer</name>
       <supportAction>
         <action>start</action>
       </supportAction>
       <icon_url></icon_url>
     </app>
 * }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class DialApp {

    /** The application identifier */
    private @Nullable String id;

    /** The name of the application */
    private @Nullable String name;

    /** The url to the application icon */
    @XStreamAlias("icon_url")
    private @Nullable String iconUrl;

    /** The actions supported by the application */
    @XStreamAlias("supportAction")
    private @Nullable SupportedAction supportedAction;

    /**
     * Gets the application id
     *
     * @return a possibly null, possibly empty application id
     */
    public @Nullable String getId() {
        return id;
    }

    /**
     * Gets the name of the application
     *
     * @return a possibly null, possibly empty application name
     */
    public @Nullable String getName() {
        return name;
    }

    /**
     * Gets the application's icon URL
     *
     * @return a possibly null, possibly empty application icon URL
     */
    public @Nullable String getIconUrl() {
        return iconUrl;
    }

    /**
     * Gets the actions supported by the application
     *
     * @return the non-null, possibly empty list of application actions
     */
    public List<String> getActions() {
        final SupportedAction localSupportedAction = supportedAction;
        return localSupportedAction == null || localSupportedAction.actions == null ? Collections.emptyList()
                : Collections.unmodifiableList(localSupportedAction.actions);
    }

    /**
     * Internal class used simply for deserializing the supported actions. Note: this class is not private since
     * DialXmlReader needs access to the class (to process the annotations)
     *
     * @author Tim Roberts - Initial contribution
     */
    @NonNullByDefault
    class SupportedAction {
        @XStreamImplicit(itemFieldName = "action")
        private @Nullable List<String> actions;
    }
}
