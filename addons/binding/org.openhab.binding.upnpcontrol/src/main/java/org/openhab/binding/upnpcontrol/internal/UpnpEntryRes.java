/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upnpcontrol.internal;

/**
 *
 * @author Mark Herwege - Initial contribution
 */
class UpnpEntryRes {

    private String protocolInfo;
    private Integer size;
    private String importUri;
    private String res;

    UpnpEntryRes(String protocolInfo, Integer size, String importUri) {
        this.protocolInfo = protocolInfo;
        this.size = size;
        this.importUri = importUri;
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
        this.res = res;
    }

    public String getProtocolInfo() {
        return protocolInfo;
    }

    /**
     * @return the size
     */
    public Integer getSize() {
        return size;
    }

    /**
     * @return the importUri
     */
    public String getImportUri() {
        return importUri;
    }
}
