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
