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

/**
 * The system control protocol class is used to control basic non-zone functionality
 * of a Yamaha receiver with HTTP/xml.
 * No state will be saved in here, but in {@link SystemControlState} instead.
 *
 * @author David Gräff - Initial contribution
 * @author Tomasz Maruszak - refactoring
 */
public class SystemControlXML implements SystemControl {
    private WeakReference<AbstractConnection> comReference;
    private SystemControlStateListener observer;

    public SystemControlXML(AbstractConnection xml, SystemControlStateListener observer) {
        this.comReference = new WeakReference<>(xml);
        this.observer = observer;
    }

    @Override
    public void update() throws IOException, ReceivedMessageParseException {
        if (observer == null) {
            return;
        }

        AbstractConnection xml = comReference.get();
        String response = xml.sendReceive("<System><Power_Control>GetParam</Power_Control></System>");
        Document doc = XMLUtils.xml(response);
        if (doc == null || doc.getFirstChild() == null) {
            throw new ReceivedMessageParseException("<System><Power_Control>GetParam failed: " + response);
        }

        Node basicStatus = XMLUtils.getNode(doc.getFirstChild(), "System/Power_Control");

        SystemControlState state = new SystemControlState();

        String value = XMLUtils.getNodeContentOrDefault(basicStatus, "Power", "");
        state.power = value.equals("On");

        observer.systemControlStateChanged(state);
    }

    @Override
    public void setPower(boolean power) throws IOException, ReceivedMessageParseException {
        String str = power ? "On" : "Standby";
        comReference.get().send("<System><Power_Control><Power>" + str + "</Power></Power_Control></System>");
        update();
    }
}
