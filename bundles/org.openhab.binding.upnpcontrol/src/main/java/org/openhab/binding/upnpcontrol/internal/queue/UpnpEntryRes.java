/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.upnpcontrol.internal.queue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class UpnpEntryRes {

    private String protocolInfo;
    private @Nullable Long size;
    private String duration;
    private String importUri;
    private String res = "";

    public UpnpEntryRes(String protocolInfo, @Nullable Long size, @Nullable String duration,
            @Nullable String importUri) {
        this.protocolInfo = protocolInfo.trim();
        this.size = size;
        this.duration = (duration == null) ? "" : duration.trim();
        this.importUri = (importUri == null) ? "" : importUri.trim();
    }

    /**
     * @return the res
     */
    public String getRes() {
        return res;
    }

    /**
     * @param res the res to set
     */
    public void setRes(String res) {
        this.res = res.trim();
    }

    public String getProtocolInfo() {
        return protocolInfo;
    }

    /**
     * @return the size
     */
    public @Nullable Long getSize() {
        return size;
    }

    /**
     * @return the duration
     */
    public String getDuration() {
        return duration;
    }

    /**
     * @return the importUri
     */
    public String getImportUri() {
        return importUri;
    }

    /**
     * @return true if this resource defines a thumbnail as specified in the DLNA specs
     */
    public boolean isThumbnailRes() {
        return getProtocolInfo().toLowerCase().contains("dlna.org_pn=jpeg_tn");
    }
}
