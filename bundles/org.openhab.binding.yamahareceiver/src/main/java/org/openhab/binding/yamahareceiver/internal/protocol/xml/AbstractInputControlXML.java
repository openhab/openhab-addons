/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Inputs.*;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Feature;
import org.openhab.binding.yamahareceiver.internal.config.YamahaUtils;
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.slf4j.Logger;

/**
 * Provides basis for all input controls
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public abstract class AbstractInputControlXML {

    protected final Logger logger;

    protected final WeakReference<AbstractConnection> comReference;
    protected final String inputID;
    protected final Map<String, String> inputToElement;
    protected final DeviceDescriptorXML deviceDescriptor;

    protected String inputElement;
    protected DeviceDescriptorXML.FeatureDescriptor inputFeatureDescriptor;

    private Map<String, String> loadMapping() {
        Map<String, String> map = new HashMap<>();

        // ToDo: For maximum compatibility these should be obtained fro the VNC_Tag of the desc.xml
        map.put(INPUT_TUNER, "Tuner");
        map.put(INPUT_NET_RADIO, "NET_RADIO");
        map.put(INPUT_MUSIC_CAST_LINK, "MusicCast_Link");

        return map;
    }

    protected AbstractInputControlXML(Logger logger, String inputID, AbstractConnection con,
            DeviceInformationState deviceInformationState) {
        this.logger = logger;
        this.comReference = new WeakReference<>(con);
        this.inputID = inputID;
        this.deviceDescriptor = DeviceDescriptorXML.getAttached(deviceInformationState);
        this.inputToElement = loadMapping();
        this.inputElement = inputToElement.getOrDefault(inputID, inputID);
        this.inputFeatureDescriptor = getInputFeatureDescriptor();
    }

    /**
     * Wraps the XML message with the inputID tags. Example with inputID=NET_RADIO:
     * <NET_RADIO>message</NET_RADIO>.
     *
     * @param message XML message
     * @return
     */
    protected String wrInput(String message) {
        return String.format("<%s>%s</%s>", inputElement, message, inputElement);
    }

    protected DeviceDescriptorXML.FeatureDescriptor getInputFeatureDescriptor() {
        if (deviceDescriptor == null) {
            logger.trace("Descriptor not available");
            return null;
        }

        Feature inputFeature = YamahaUtils.tryParseEnum(Feature.class, inputElement);

        // For RX-V3900 both the inputs 'NET RADIO' and 'USB' need to use the same NET_USB element
        if ((INPUT_NET_RADIO.equals(inputID) || INPUT_USB.equals(inputID))
                && deviceDescriptor.features.containsKey(Feature.NET_USB)
                && !deviceDescriptor.features.containsKey(Feature.NET_RADIO)
                && !deviceDescriptor.features.containsKey(Feature.USB)) {

            // have to use the NET_USB xml element in this case
            inputElement = "NET_USB";
            inputFeature = Feature.NET_USB;
        }

        if (inputFeature != null) {
            return deviceDescriptor.features.getOrDefault(inputFeature, null);
        }
        return null;
    }
}
