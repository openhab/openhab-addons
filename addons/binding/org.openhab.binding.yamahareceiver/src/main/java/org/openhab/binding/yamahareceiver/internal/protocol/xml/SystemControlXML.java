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

import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.protocol.SystemControl;
import org.openhab.binding.yamahareceiver.internal.state.SystemControlState;
import org.openhab.binding.yamahareceiver.internal.state.SystemControlStateListener;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLConstants.ON;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLConstants.POWER_STANDBY;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLUtils.getNode;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLUtils.getNodeContentOrDefault;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLUtils.getNodeContentOrEmpty;

/**
 * The system control protocol class is used to control basic non-zone functionality
 * of a Yamaha receiver with HTTP/xml.
 * No state will be saved in here, but in {@link SystemControlState} instead.
 *
 * @author David Gräff - Initial contribution
 * @author Tomasz Maruszak - refactoring, HTR-xxxx Zone_2 compatibility
 *
 */
public class SystemControlXML implements SystemControl {
    private WeakReference<AbstractConnection> comReference;
    private SystemControlStateListener observer;

    protected String setPowerCmd = "<System><Power_Control><Power>%s</Power></Power_Control></System>";
    protected String setPowerPath = "Power_Control/Power";

    public SystemControlXML(AbstractConnection xml, SystemControlStateListener observer) {
        this.comReference = new WeakReference<>(xml);
        this.observer = observer;
    }

    @Override
    public void setPower(boolean power) throws IOException, ReceivedMessageParseException {
        String cmd = String.format(setPowerCmd, power ? ON : POWER_STANDBY);
        comReference.get().send(cmd);
        update();
    }

    @Override
    public void update() throws IOException, ReceivedMessageParseException {
        if (observer == null) {
            return;
        }

        Node basicStatus = XMLProtocolService.getResponse(comReference.get(),
                "<System><Power_Control>GetParam</Power_Control></System>",
                "System");

        SystemControlState state = new SystemControlState();

        String value = getNodeContentOrEmpty(basicStatus, setPowerPath);
        state.power = ON.equals(value);

        observer.systemControlStateChanged(state);
    }
}
