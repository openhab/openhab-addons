/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.freeboxos.internal.api.ApiConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.Permission;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.lan.browser.LanBrowserManager.HostsResponse;
import org.openhab.binding.freeboxos.internal.api.lan.browser.LanHost;
import org.openhab.binding.freeboxos.internal.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.rest.ListableRest;

/**
 * The {@link RepeaterManager} is the Java class used to handle api requests related to repeater
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class RepeaterManager extends ListableRest<Repeater, RepeaterManager.RepeaterResponse> {
    public static class RepeaterResponse extends Response<Repeater> {
    }

    public RepeaterManager(FreeboxOsSession session) throws FreeboxException {
        super(session, Permission.NONE, RepeaterResponse.class, session.getUriBuilder().path(REPEATER_SUB_PATH));
    }

    public List<LanHost> getRepeaterHosts(int id) throws FreeboxException {
        return get(HostsResponse.class, Integer.toString(id), HOST_SUB_PATH);
    }

    public synchronized List<LanHost> getHosts() throws FreeboxException {
        List<LanHost> hosts = new ArrayList<>();
        for (Repeater rep : getDevices()) {
            hosts.addAll(getRepeaterHosts(rep.getId()));
        }
        return hosts;
    }

    public Optional<LanHost> getHost(String mac) throws FreeboxException {
        return getHosts().stream().filter(host -> mac.equalsIgnoreCase(host.getMac())).findFirst();
    }

    public void reboot(int id) throws FreeboxException {
        post(Integer.toString(id), REBOOT_SUB_PATH);
    }

    public Optional<Repeater> led(int id, boolean enable) throws FreeboxException {
        Repeater config = getDevice(id);
        config.setLedActivated(enable);
        Repeater result = put(RepeaterResponse.class, config, Integer.toString(id));
        return Optional.ofNullable(result);
    }

}
