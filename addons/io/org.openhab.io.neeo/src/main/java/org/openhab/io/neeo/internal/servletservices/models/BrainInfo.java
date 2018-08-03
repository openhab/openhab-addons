/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.servletservices.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The brain information class. This class will be used to communicate the brain ID or IP address between the
 * integration
 * page and the servlet
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class BrainInfo {
    /** The possibly null, possibly empty brain identifier (used when removing a brain) */
    @Nullable
    private String brainId;
    /** The possibly null, possibly empty brain ip address (used when adding a brain) */
    @Nullable
    private String brainIp;

    /**
     * Returns the brain identifier
     *
     * @return a possibly null, possibly empty brain ID
     */
    @Nullable
    public String getBrainId() {
        return brainId;
    }

    /**
     * Returns the brain ip address
     *
     * @return a possibly null, possibly empty brain ip address
     */
    @Nullable
    public String getBrainIp() {
        return brainIp;
    }

    @Override
    public String toString() {
        return "BrainId [brainId=" + brainId + ", brainIp=" + brainIp + "]";
    }
}
