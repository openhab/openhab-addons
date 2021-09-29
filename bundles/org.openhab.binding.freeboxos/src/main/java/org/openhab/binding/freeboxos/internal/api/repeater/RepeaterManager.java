/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.repeater;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.lan.LanHost;
import org.openhab.binding.freeboxos.internal.api.lan.LanHost.LanHostsResponse;
import org.openhab.binding.freeboxos.internal.api.repeater.Repeater.RepeaterResponse;
import org.openhab.binding.freeboxos.internal.api.repeater.Repeater.RepeatersResponse;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.rest.ListableRest;

/**
 * The {@link RepeaterManager} is the Java class used to handle api requests
 * related to repeater
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class RepeaterManager extends ListableRest<Repeater, RepeaterResponse, RepeatersResponse> {
    private static final String HOST_SUB_PATH = "host";
    private final static String REPEATER_SUB_PATH = "repeater";

    public RepeaterManager(FreeboxOsSession session) throws FreeboxException {
        super(REPEATER_SUB_PATH, session, RepeaterResponse.class, RepeatersResponse.class);
    }

    public List<LanHost> getRepeaterHosts(int id) throws FreeboxException {
        UriBuilder myBuilder = getUriBuilder();
        myBuilder.path(Integer.toString(id)).path(HOST_SUB_PATH);
        return getList(LanHostsResponse.class, myBuilder.build());
    }

    private synchronized List<LanHost> getHosts() throws FreeboxException {
        List<LanHost> hosts = new ArrayList<>();
        for (Repeater rep : getDevices()) {
            List<LanHost> repHosts = getRepeaterHosts(rep.getId());
            hosts.addAll(repHosts);
        }
        return hosts;
    }

    public Map<String, @Nullable LanHost> getHostsMap() throws FreeboxException {
        Map<String, @Nullable LanHost> result = new HashMap<>();
        getHosts().stream().forEach(host -> {
            String mac = host.getMac();
            if (mac != null) {
                result.put(mac, host);
            }
        });
        return result;
    }
}
