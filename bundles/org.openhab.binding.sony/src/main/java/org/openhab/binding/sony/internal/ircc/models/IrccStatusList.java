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
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * This class represents the status list element in the XML for a IRCC device. The XML that will be deserialized will
 * look like:
 *
 * <pre>
 * {@code
    <?xml version="1.0" encoding="UTF-8"?>
    <statusList>
      <status name="disc">
        <statusItem field="type" value="DVD"/>
        <statusItem field="mediaType" value="DVD"/>
        <statusItem field="mediaFormat" value="VIDEO"/>
      </status>
    </statusList>
 * }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@XStreamAlias("statusList")
public class IrccStatusList {

    /** The list of statuses */
    @XStreamImplicit
    private @Nullable List<@Nullable IrccStatus> statuses;

    /**
     * Parses's the {@link IrccStatusList} from the XML
     *
     * @param xml the non-null, non-empty XML to parse
     * @return a possibly null {@link IrccStatusList}
     */
    public static @Nullable IrccStatusList get(final String xml) {
        Validate.notEmpty(xml, "xml cannot be empty");
        return IrccXmlReader.STATUS.fromXML(xml);
    }

    /**
     * Returns true if the status is a {@link IrccStatus#TEXTINPUT}
     *
     * @return true if a text input, false otherwise
     */
    public boolean isTextInput() {
        final List<@Nullable IrccStatus> localStatuses = statuses;
        if (localStatuses != null) {
            for (final IrccStatus status : localStatuses) {
                if (status != null && StringUtils.equalsIgnoreCase(IrccStatus.TEXTINPUT, status.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the status is a {@link IrccStatus#WEBBROWSER}
     *
     * @return true if a web browser, false otherwise
     */
    public boolean isWebBrowse() {
        final List<@Nullable IrccStatus> localStatuses = statuses;
        if (localStatuses != null) {
            for (final IrccStatus status : localStatuses) {
                if (status != null && StringUtils.equalsIgnoreCase(IrccStatus.WEBBROWSER, status.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the status is a {@link IrccStatus#DISC}
     *
     * @return true if a disk, false otherwise
     */
    public boolean isDisk() {
        final List<@Nullable IrccStatus> localStatuses = statuses;
        if (localStatuses != null) {
            for (final IrccStatus status : localStatuses) {
                if (status != null && StringUtils.equalsIgnoreCase(IrccStatus.DISC, status.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the viewing status
     *
     * @return the possibly null (if not {@link IrccStatus#VIEWING}) {@link IrccStatus}
     */
    public @Nullable IrccStatus getViewing() {
        final List<@Nullable IrccStatus> localStatuses = statuses;
        if (localStatuses != null) {
            for (final IrccStatus status : localStatuses) {
                if (status != null && StringUtils.equalsIgnoreCase(IrccStatus.VIEWING, status.getName())) {
                    return status;
                }
            }
        }
        return null;
    }
}
