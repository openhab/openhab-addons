/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.function.Supplier;

import org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Zone;
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.InputConverter;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.protocol.ZoneAvailableInputs;
import org.openhab.binding.yamahareceiver.internal.state.AvailableInputState;
import org.openhab.binding.yamahareceiver.internal.state.AvailableInputStateListener;
import org.openhab.binding.yamahareceiver.internal.state.ZoneControlState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public ZoneAvailableInputsXML(AbstractConnection con, Zone zone, AvailableInputStateListener observer,
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

    @Override
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
            logger.trace("Zone {} - available inputs: {}", getZone(),
                    state.availableInputs.keySet().stream().collect(joining(", ")));
        }

        observer.availableInputsChanged(state);
    }
}
