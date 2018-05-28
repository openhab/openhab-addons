/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import static org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.Inputs.INPUT_MUSIC_CAST_LINK;
import static org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.Inputs.INPUT_NET_RADIO;
import static org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.Inputs.INPUT_TUNER;

/**
 * Provides basis for all input controls
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public abstract class AbstractInputControlXML {

    protected final WeakReference<AbstractConnection> comReference;
    protected final String inputID;
    protected final Map<String, String> inputToElement;

    private Map<String, String> loadMapping() {
        Map<String, String> map = new HashMap<>();

        // ToDo: For maximum compatibility these should be obtained fro the VNC_Tag of the desc.xml
        map.put(INPUT_TUNER, "Tuner");
        map.put(INPUT_NET_RADIO, "NET_RADIO");
        map.put(INPUT_MUSIC_CAST_LINK, "MusicCast_Link");

        return map;
    }

    protected AbstractInputControlXML(String inputID, AbstractConnection com) {
        this.comReference = new WeakReference<>(com);
        this.inputID = inputID;
        this.inputToElement = loadMapping();
    }

    /**
     * Wraps the XML message with the inputID tags. Example with inputID=NET_RADIO:
     * <NET_RADIO>message</NET_RADIO>.
     *
     * @param message XML message
     * @return
     */
    protected String wrInput(String message) {
        String elementName = getInputElement();
        return String.format("<%s>%s</%s>", elementName, message, elementName);
    }

    protected String getInputElement() {
        return inputToElement.getOrDefault(inputID, inputID);
    }
}
