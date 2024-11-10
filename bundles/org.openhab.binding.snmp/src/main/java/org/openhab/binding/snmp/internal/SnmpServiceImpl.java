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
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.snmp.internal.config.SnmpServiceConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.net.CidrAddress;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.AuthHMAC128SHA224;
import org.snmp4j.security.AuthHMAC192SHA256;
import org.snmp4j.security.AuthHMAC256SHA384;
import org.snmp4j.security.AuthHMAC384SHA512;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivAES192;
import org.snmp4j.security.PrivAES256;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
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
public class SnmpServiceImpl implements SnmpService, NetworkAddressChangeListener {
    private final Logger logger = LoggerFactory.getLogger(SnmpServiceImpl.class);

    private @Nullable Snmp snmp;
    private @Nullable DefaultUdpTransportMapping transport;
    private final NetworkAddressService networkAddressService;

    private final List<CommandResponder> listeners = new ArrayList<>();
    private final Set<UserEntry> userEntries = new HashSet<>();
    private Map<String, Object> config = new HashMap<>();

    @Activate
    public SnmpServiceImpl(Map<String, Object> config, @Reference NetworkAddressService networkAddressService) {
        addProtocols();
        OctetString localEngineId = new OctetString(MPv3.createLocalEngineID());
        USM usm = new USM(SecurityProtocols.getInstance(), localEngineId, 0);
        SecurityModels.getInstance().addSecurityModel(usm);
        this.networkAddressService = networkAddressService;
        networkAddressService.addNetworkAddressChangeListener(this);
        modified(config);
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        this.config = config;
        SnmpServiceConfiguration snmpCfg = new Configuration(config).as(SnmpServiceConfiguration.class);
        try {
            shutdownSnmp();

            final DefaultUdpTransportMapping transport;

            if (snmpCfg.port > 0) {
                InetAddress inetAddress = Objects.requireNonNullElse(
                        InetAddress.getByName(networkAddressService.getPrimaryIpv4HostAddress()),
                        InetAddress.getLocalHost());
                transport = new DefaultUdpTransportMapping(new UdpAddress(inetAddress, snmpCfg.port), true);
            } else {
                transport = new DefaultUdpTransportMapping();
            }

            addProtocols();

            final Snmp snmp = new Snmp(transport);
            listeners.forEach(snmp::addCommandResponder);
            snmp.listen();

            // re-add user entries
            userEntries.forEach(u -> snmp.getUSM().addUser(u.user, u.engineId));

            this.snmp = snmp;
            this.transport = transport;

            logger.info("Initialized SNMP service at {}", transport.getAddress());
        } catch (IOException e) {
            logger.warn("could not open SNMP instance on port {}: {}", snmpCfg.port, e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    @Deactivate
    public void deactivate() {
        try {
            shutdownSnmp();
        } catch (IOException e) {
            logger.warn("Could not end SNMP service: {}", e.getMessage());
        }
    }

    private void addProtocols() {
        SecurityProtocols secProtocols = SecurityProtocols.getInstance();
        secProtocols.addAuthenticationProtocol(new AuthMD5());
        secProtocols.addAuthenticationProtocol(new AuthSHA());
        secProtocols.addAuthenticationProtocol(new AuthHMAC128SHA224());
        secProtocols.addAuthenticationProtocol(new AuthHMAC192SHA256());
        secProtocols.addAuthenticationProtocol(new AuthHMAC256SHA384());
        secProtocols.addAuthenticationProtocol(new AuthHMAC384SHA512());
        secProtocols.addPrivacyProtocol(new PrivDES());
        secProtocols.addPrivacyProtocol(new Priv3DES());
        secProtocols.addPrivacyProtocol(new PrivAES128());
        secProtocols.addPrivacyProtocol(new PrivAES192());
        secProtocols.addPrivacyProtocol(new PrivAES256());
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
    public void send(PDU pdu, Target<?> target, @Nullable Object userHandle, ResponseListener listener)
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
    public void addUser(UsmUser user, OctetString engineId) {
        UserEntry userEntry = new UserEntry(user, engineId);
        userEntries.add(userEntry);

        Snmp snmp = this.snmp;
        if (snmp != null) {
            snmp.getUSM().addUser(user, engineId);
        }
    }

    @Override
    public void removeUser(Address address, UsmUser user, OctetString engineId) {
        Snmp snmp = this.snmp;
        if (snmp != null) {
            snmp.getUSM().removeAllUsers(user.getSecurityName(), engineId);
            snmp.removeCachedContextEngineId(address);
        }
        userEntries.removeIf(e -> e.engineId.equals(engineId) && e.user.equals(user));
    }

    @Override
    public byte @Nullable [] getEngineId(Address address) {
        Snmp snmp = this.snmp;
        if (snmp != null) {
            return snmp.discoverAuthoritativeEngineID(address, 15000);
        }
        return null;
    }

    private static class UserEntry {
        public OctetString engineId;
        public UsmUser user;

        public UserEntry(UsmUser user, OctetString engineId) {
            this.engineId = engineId;
            this.user = user;
        }
    }

    @Override
    public void onChanged(List<CidrAddress> added, List<CidrAddress> removed) {
        logger.trace("SNMP reacting on network interface changes.");
        modified(this.config);
    }
}
