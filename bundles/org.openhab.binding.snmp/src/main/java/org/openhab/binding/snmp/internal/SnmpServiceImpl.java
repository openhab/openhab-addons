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
package org.openhab.binding.snmp.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.snmp.internal.config.SnmpServiceConfiguration;
import org.openhab.binding.snmp.internal.types.SnmpAuthProtocol;
import org.openhab.binding.snmp.internal.types.SnmpPrivProtocol;
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
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OctetString;
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

    private final List<CommandResponder> listeners = new ArrayList<>();
    private final Set<UserEntry> userEntries = new HashSet<>();

    @Activate
    public SnmpServiceImpl(Map<String, Object> config) {
        SecurityProtocols.getInstance().addDefaultProtocols();
        SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());

        OctetString localEngineId = new OctetString(MPv3.createLocalEngineID());
        USM usm = new USM(SecurityProtocols.getInstance(), localEngineId, 0);
        SecurityModels.getInstance().addSecurityModel(usm);

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
            listeners.forEach(snmp::addCommandResponder);
            snmp.listen();

            // re-add user entries
            userEntries.forEach(u -> addUser(snmp, u));

            this.snmp = snmp;
            this.transport = transport;

            logger.debug("initialized SNMP at {}", transport.getAddress());
        } catch (IOException e) {
            logger.warn("could not open SNMP instance on port {}: {}", this.config.port, e.getMessage());
        }
    }

    @SuppressWarnings("unused")
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

    @Override
    public void addUser(String userName, SnmpAuthProtocol snmpAuthProtocol, @Nullable String authPassphrase,
            SnmpPrivProtocol snmpPrivProtocol, @Nullable String privPassphrase, byte[] engineId) {
        UsmUser usmUser = new UsmUser(new OctetString(userName), snmpAuthProtocol.getOid(),
                authPassphrase != null ? new OctetString(authPassphrase) : null, snmpPrivProtocol.getOid(),
                privPassphrase != null ? new OctetString(privPassphrase) : null);
        OctetString securityNameOctets = new OctetString(userName);

        UserEntry userEntry = new UserEntry(securityNameOctets, new OctetString(engineId), usmUser);
        userEntries.add(userEntry);

        Snmp snmp = this.snmp;
        if (snmp != null) {
            addUser(snmp, userEntry);
        }
    }

    private static void addUser(Snmp snmp, UserEntry userEntry) {
        snmp.getUSM().addUser(userEntry.securityName, userEntry.engineId, userEntry.user);
    }

    private static class UserEntry {
        public OctetString securityName;
        public OctetString engineId;
        public UsmUser user;

        public UserEntry(OctetString securityName, OctetString engineId, UsmUser user) {
            this.securityName = securityName;
            this.engineId = engineId;
            this.user = user;
        }
    }
}
