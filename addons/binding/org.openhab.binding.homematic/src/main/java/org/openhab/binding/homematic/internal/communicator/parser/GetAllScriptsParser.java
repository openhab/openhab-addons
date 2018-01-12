/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.parser;

import java.io.IOException;

import org.apache.commons.lang.ObjectUtils;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.binding.homematic.internal.model.HmValueType;

/**
 * Parses a Homegear message with scripts and generates the scripts.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GetAllScriptsParser extends CommonRpcParser<Object[], Void> {
    private HmChannel channel;

    public GetAllScriptsParser(HmChannel channel) {
        this.channel = channel;
    }

    @Override
    public Void parse(Object[] message) throws IOException {
        message = (Object[]) message[0];
        for (int i = 0; i < message.length; i++) {
            String scriptName = ObjectUtils.toString(message[i]);
            HmDatapoint dpScript = new HmDatapoint(scriptName, scriptName, HmValueType.BOOL, Boolean.FALSE, false,
                    HmParamsetType.VALUES);
            dpScript.setInfo(scriptName);
            channel.addDatapoint(dpScript);
        }
        return null;
    }

}
