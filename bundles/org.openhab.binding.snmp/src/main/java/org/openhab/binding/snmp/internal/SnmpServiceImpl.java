/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.snmp.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.snmp.internal.config.SnmpServiceConfiguration;
import org.openhab.core.config.core.Configuration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 * The {@link SnmpServiceImpl} implements SnmpService
 * handlers.
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
@Component(configurationPid = "binding.snmp", service = SnmpService.class)
public class SnmpServiceImpl implements SnmpService {
    private final Logger logger = LoggerFactory.getLogger(SnmpServiceImpl.class);

    private @NonNullByDefault({}) SnmpServiceConfiguration config;
    private @Nullable Snmp snmp;
    private @Nullable DefaultUdpTransportMapping transport;

    private List<CommandResponder> listeners = new ArrayList<>();

    @Activate
    public SnmpServiceImpl(Map<String, Object> config) {
        modified(config);
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        this.config = new Configuration(config).as(SnmpServiceConfiguration.class);
        try {
            shutdownSnmp();

            final DefaultUdpTransportMapping transport;

            if (this.config.port > 0) {
                transport = new DefaultUdpTransportMapping(new UdpAddress(this.config.port), true);
            } else {
                transport = new DefaultUdpTransportMapping();
            }

            SecurityProtocols.getInstance().addDefaultProtocols();
            SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());

            final Snmp snmp = new Snmp(transport);
            listeners.forEach(listener -> snmp.addCommandResponder(listener));
            snmp.listen();

            this.snmp = snmp;
            this.transport = transport;

            logger.debug("initialized SNMP at {}", transport.getAddress());
        } catch (IOException e) {
            logger.warn("could not open SNMP instance on port {}: {}", this.config.port, e.getMessage());
        }
    }

    @Deactivate
    public void deactivate() {
        try {
            shutdownSnmp();
        } catch (IOException e) {
            logger.info("could not end SNMP: {}", e.getMessage());
        }
    }

    private void shutdownSnmp() throws IOException {
        DefaultUdpTransportMapping transport = this.transport;
        if (transport != null) {
            transport.close();
            this.transport = null;
        }
        Snmp snmp = this.snmp;
        if (snmp != null) {
            snmp.close();
            this.snmp = null;
        }
    }

    @Override
    public void addCommandResponder(CommandResponder listener) {
        Snmp snmp = this.snmp;
        if (snmp != null) {
            snmp.addCommandResponder(listener);
        }
        listeners.add(listener);
    }

    @Override
    public void removeCommandResponder(CommandResponder listener) {
        Snmp snmp = this.snmp;
        if (snmp != null) {
            snmp.removeCommandResponder(listener);
        }
        listeners.remove(listener);
    }

    @Override
    public void send(PDU pdu, Target target, @Nullable Object userHandle, ResponseListener listener)
            throws IOException {
        Snmp snmp = this.snmp;
        if (snmp != null) {
            snmp.send(pdu, target, userHandle, listener);
            logger.trace("send {} to {}", pdu, target);
        } else {
            logger.warn("SNMP service not initialized, can't send {} to {}", pdu, target);
        }
    }
}
