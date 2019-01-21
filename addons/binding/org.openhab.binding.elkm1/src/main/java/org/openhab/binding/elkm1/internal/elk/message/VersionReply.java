/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.elkm1.internal.elk.message;

import org.openhab.binding.elkm1.internal.elk.ElkCommand;
import org.openhab.binding.elkm1.internal.elk.ElkMessage;

/**
 * The result from elk with the version details in it.
 *
 * @author David Bennett - Initial Contribution
 */
public class VersionReply extends ElkMessage {
    private String versionMax;
    private String versionMiddle;
    private String versionLow;

    public VersionReply(String incomingData) {
        super(ElkCommand.RequestVersionNumberReply);
        versionMax = incomingData.substring(0, 2);
        versionMiddle = incomingData.substring(2, 4);
        versionLow = incomingData.substring(4, 6);
    }

    /**
     * The version number of this elk.
     */
    public String getElkVersion() {
        return versionMax + "." + versionMiddle + "." + versionLow;
    }

    @Override
    protected String getData() {
        return null;
    }

}
