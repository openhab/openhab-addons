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
package org.openhab.io.hueemulation.internal.rest;

import java.net.URI;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.UpnpService;
import org.jupnp.controlpoint.ControlPoint;
import org.jupnp.model.meta.Device;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.LocalDevice;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.registry.Registry;
import org.jupnp.registry.RegistryListener;
import org.openhab.io.hueemulation.internal.ConfigStore;
import org.openhab.io.hueemulation.internal.HueEmulationService;
import org.openhab.io.hueemulation.internal.upnp.UpnpServer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationSelect;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used by the status REST API for troubleshoot purposes.
 * <p>
 * The UPNP announcement is tested, the /description.xml reachability is checked,
 * and some statistics are gathered.
 *
 * @author David Graeff - Initial contribution
 */
@Component(immediate = false, service = StatusResource.class)
@JaxrsResource
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + HueEmulationService.REST_APP_NAME + ")")
@NonNullByDefault
@Path("")
public class StatusResource implements RegistryListener {
    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
    protected @Nullable UpnpServer discovery;
    @Reference
    protected @NonNullByDefault({}) ConfigStore cs;
    @Reference
    protected @NonNullByDefault({}) UpnpService upnpService;

    private enum UpnpStatus {
        service_not_registered,
        service_registered_but_no_UPnP_traffic_yet,
        upnp_announcement_thread_not_running,
        any_device_but_not_this_one_found,
        success
    }

    private UpnpStatus selfTestUpnpFound = UpnpStatus.service_not_registered;

    private final Logger logger = LoggerFactory.getLogger(StatusResource.class);

    /**
     * This will register to the {@link UpnpService} registry to get notified of new UPNP devices and check all existing
     * devices if they match our UPNP announcement.
     */
    public void startUpnpSelfTest() {
        Registry registry = upnpService.getRegistry();
        if (registry == null) {
            logger.warn("upnp service registry is null!");
            return;
        }

        selfTestUpnpFound = UpnpStatus.service_registered_but_no_UPnP_traffic_yet;

        for (RemoteDevice device : registry.getRemoteDevices()) {
            remoteDeviceAdded(registry, device);
        }
        for (LocalDevice device : registry.getLocalDevices()) {
            localDeviceAdded(registry, device);
        }

        registry.addListener(this);

        ControlPoint controlPoint = upnpService.getControlPoint();
        if (controlPoint != null) {
            controlPoint.search();
        }
    }

    @GET
    @Path("status/link")
    @Produces("text/plain")
    public Response link(@Context UriInfo uri) {
        cs.setLinkbutton(!cs.ds.config.linkbutton, true, uri.getQueryParameters().containsKey("v1"));
        URI newuri = uri.getBaseUri().resolve("/api/status");
        return Response.seeOther(newuri).build();
    }

    private static String toYesNo(boolean b) {
        return b ? "yes" : "no";
    }

    private static String TD(String s) {
        return "<td>" + s + "</td>";
    }

    private static String TR(String s) {
        return "<tr>" + s + "</tr>";
    }

    @GET
    @Path("status")
    @Produces("text/html")
    public String getStatus() {
        UpnpServer localDiscovery = discovery;
        if (localDiscovery == null) { // Optional service wiring
            return "UPnP Server service not started!";
        }

        String format = "<html><body><h1>Self test</h1>" + //
                "<p>To access any links you need be in pairing mode!</p>" + //
                "<p>Pairing mode: %s (%s) <a href='%s/api/status/link'>Enable</a> | <a href='%s/api/status/link?V1=true'>Enable with bridge V1 emulation</a></p>"
                + //
                "%d published lights (see <a href='%s/api/testuser/lights'>%s/api/testuser/lights</a>)<br>" + //
                "%d published sensors (see <a href='%s/api/testuser/sensors'>%s/api/testuser/sensors</a>)<br>" + //
                "<h2>UPnP discovery test</h2>" + //
                "<p>%s</p>" + //
                "<table style='border:1px solid black'><tr><td>serial no</td><td>name</td></tr>%s</table>" + //
                "<h2>Reachability test</h2>" + //
                "<table style='border:1px solid black'><tr><td>URL</td><td>Responds?</td><td>Ours?</td></tr>%s</table>"
                + //
                "<h2>Users</h2><ul>%s</ul></body></html>";

        String users = cs.ds.config.whitelist.entrySet().stream().map(user -> "<li>" + user.getKey() + " <b>"
                + user.getValue().name + "</b> <small>" + user.getValue().lastUseDate + "</small>")
                .collect(Collectors.joining("\n"));

        String url = "http://" + cs.ds.config.ipaddress + ":" + localDiscovery.getDefaultport();

        String reachable = localDiscovery.selfTests().stream()
                .map(entry -> TR(TD(entry.address) + TD(toYesNo(entry.reachable)) + TD(toYesNo(entry.isOurs))))
                .collect(Collectors.joining("\n"));

        Registry registry = upnpService.getRegistry();
        String upnps;
        if (registry != null) {
            upnps = registry.getRemoteDevices().stream().map(device -> getDetails(device))
                    .map(details -> TR(TD(details.getSerialNumber()) + TD(details.getFriendlyName())))
                    .collect(Collectors.joining("\n"));
        } else {
            upnps = TR(TD("service not available") + TD(""));
        }

        if (!localDiscovery.upnpAnnouncementThreadRunning()) {
            selfTestUpnpFound = UpnpStatus.upnp_announcement_thread_not_running;
        }

        return String.format(format, cs.ds.config.linkbutton ? "On" : "Off",
                cs.getConfig().temporarilyEmulateV1bridge ? "V1" : "V2", url, url, //
                cs.ds.lights.size(), url, url, cs.ds.sensors.size(), url, url, //
                selfTestUpnpFound.name().replace('_', ' '), //
                upnps, reachable, users);
    }

    @NonNullByDefault({})
    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
    }

    @NonNullByDefault({})
    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
    }

    @NonNullByDefault({})
    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        if (selfTestUpnpFound == UpnpStatus.success) {
            return;
        }
        checkForDevice(getDetails(device));
    }

    private static DeviceDetails getDetails(@Nullable Device<?, ?, ?> device) {
        if (device != null) {
            DeviceDetails details = device.getDetails();
            if (details != null) {
                return details;
            }
        }
        return new DeviceDetails(null, "", null, null, "", null, null, null, null, null);
    }

    private void checkForDevice(DeviceDetails details) {
        selfTestUpnpFound = UpnpStatus.any_device_but_not_this_one_found;
        try {
            if (cs.ds.config.bridgeid.equals(details.getSerialNumber())) {
                selfTestUpnpFound = UpnpStatus.success;
            }
        } catch (Exception e) { // We really don't want the service to fail on any exception
            logger.warn("upnp service: adding services failed: {}", details.getFriendlyName(), e);
        }
    }

    @NonNullByDefault({})
    @Override
    public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
    }

    @NonNullByDefault({})
    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        UpnpServer localDiscovery = discovery;
        if (selfTestUpnpFound != UpnpStatus.success || localDiscovery == null
                || localDiscovery.upnpAnnouncementThreadRunning()) {
            return;
        }
        DeviceDetails details = getDetails(device);
        String serialNo = details.getSerialNumber();
        if (cs.ds.config.bridgeid.equals(serialNo)) {
            selfTestUpnpFound = UpnpStatus.any_device_but_not_this_one_found;
        }
    }

    @NonNullByDefault({})
    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
        UpnpServer localDiscovery = discovery;
        if (selfTestUpnpFound == UpnpStatus.success || localDiscovery == null
                || localDiscovery.upnpAnnouncementThreadRunning()) {
            return;
        }
        checkForDevice(getDetails(device));
    }

    @NonNullByDefault({})
    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
    }

    @NonNullByDefault({})
    @Override
    public void beforeShutdown(Registry registry) {
    }

    @Override
    public void afterShutdown() {
        selfTestUpnpFound = UpnpStatus.service_not_registered;
    }
}
