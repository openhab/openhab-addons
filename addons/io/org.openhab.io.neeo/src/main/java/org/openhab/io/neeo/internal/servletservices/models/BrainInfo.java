/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.servletservices.models;

/**
 * The brain information class. This class will be used to communicate the brain ID or IP address between the transport
 * page and the servlet
 *
 * @author Tim Roberts - Initial contribution
 */
public class BrainInfo {
    /** The possibly null, possibly empty brain identifier (used when removing a brain) */
    private final String brainId;
    /** The possibly null, possibly empty brain ip address (used when adding a brain) */
    private final String brainIp;

    /**
     * Constructs the brain information object
     *
     * @param brainId a possibly null, possibly empty brain ID
     * @param brainIp a possibly null, possibly empty brain ip address
     */
    public BrainInfo(String brainId, String brainIp) {
        this.brainId = brainId;
        this.brainIp = brainIp;
    }

    /**
     * Returns the brain identifier
     *
     * @return a possibly null, possibly empty brain ID
     */
    public String getBrainId() {
        return brainId;
    }

    /**
     * Returns the brain ip address
     *
     * @return a possibly null, possibly empty brain ip address
     */
    public String getBrainIp() {
        return brainIp;
    }

    @Override
    public String toString() {
        return "BrainId [brainId=" + brainId + ", brainIp=" + brainIp + "]";
    }
}
