/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.Zone;
import org.openhab.binding.yamahareceiver.internal.protocol.*;
import org.openhab.binding.yamahareceiver.internal.state.AvailableInputState;
import org.openhab.binding.yamahareceiver.internal.state.AvailableInputStateListener;
import org.openhab.binding.yamahareceiver.internal.state.ZoneControlState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.joining;

/**
 * The zone protocol class is used to control one zone of a Yamaha receiver with HTTP/xml.
 * No state will be saved in here, but in {@link ZoneControlState} instead.
 *
 * @author David Gräff - Initial contribution
 * @author Tomasz Maruszak - Refactoring
 * @author Tomasz Maruszak - Input mapping fix
 *
 */
public class ZoneAvailableInputsXML implements ZoneAvailableInputs {
    protected Logger logger = LoggerFactory.getLogger(ZoneAvailableInputsXML.class);

    private final WeakReference<AbstractConnection> conReference;
    private final AvailableInputStateListener observer;
    private final Supplier<InputConverter> inputConverterSupplier;
    private final Zone zone;

    public ZoneAvailableInputsXML(AbstractConnection con,
                                  Zone zone,
                                  AvailableInputStateListener observer,
                                  Supplier<InputConverter> inputConverterSupplier) {

        this.conReference = new WeakReference<>(con);
        this.zone = zone;
        this.observer = observer;
        this.inputConverterSupplier = inputConverterSupplier;
    }

    /**
     * Return the zone
     */
    public Zone getZone() {
        return zone;
    }

    public void update() throws IOException, ReceivedMessageParseException {
        if (observer == null) {
            return;
        }

        Collection<XMLProtocolService.InputDto> inputs = XMLProtocolService.getInputs(conReference.get(), zone);

        AvailableInputState state = new AvailableInputState();

        inputs.stream().filter(XMLProtocolService.InputDto::isWritable).forEach(x -> {
            String inputName = inputConverterSupplier.get().fromStateName(x.getParam());
            state.availableInputs.put(inputName, x.getParam());
        });

        if (logger.isTraceEnabled()) {
            logger.trace("Zone {} - available inputs: {}", getZone(), state.availableInputs.keySet().stream().collect(joining(", ")));
        }

        observer.availableInputsChanged(state);
    }
}
