/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.api;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freebox.internal.api.model.AirMediaActionData;
import org.openhab.binding.freebox.internal.api.model.AirMediaActionData.MediaAction;
import org.openhab.binding.freebox.internal.api.model.AirMediaActionData.MediaType;
import org.openhab.binding.freebox.internal.api.model.AirMediaConfig;
import org.openhab.binding.freebox.internal.api.model.AirMediaConfigResponse;
import org.openhab.binding.freebox.internal.api.model.AirMediaReceiversResponse;
import org.openhab.binding.freebox.internal.api.model.AuthorizationStatusResponse;
import org.openhab.binding.freebox.internal.api.model.AuthorizeData;
import org.openhab.binding.freebox.internal.api.model.AuthorizeResponse;
import org.openhab.binding.freebox.internal.api.model.CallEntriesResponse;
import org.openhab.binding.freebox.internal.api.model.ConnectionStatusResponse;
import org.openhab.binding.freebox.internal.api.model.DiscoveryResponse;
import org.openhab.binding.freebox.internal.api.model.FtpConfig;
import org.openhab.binding.freebox.internal.api.model.FtpConfigResponse;
import org.openhab.binding.freebox.internal.api.model.FtthStatusResponse;
import org.openhab.binding.freebox.internal.api.model.LanConfigResponse;
import org.openhab.binding.freebox.internal.api.model.LanHostResponse;
import org.openhab.binding.freebox.internal.api.model.LanHostWOLData;
import org.openhab.binding.freebox.internal.api.model.LanHostsResponse;
import org.openhab.binding.freebox.internal.api.model.LanInterfacesResponse;
import org.openhab.binding.freebox.internal.api.model.LcdConfig;
import org.openhab.binding.freebox.internal.api.model.LcdConfigResponse;
import org.openhab.binding.freebox.internal.api.model.LoginResponse;
import org.openhab.binding.freebox.internal.api.model.OpenSessionData;
import org.openhab.binding.freebox.internal.api.model.OpenSessionResponse;
import org.openhab.binding.freebox.internal.api.model.PhoneConfigResponse;
import org.openhab.binding.freebox.internal.api.model.PhoneStatusResponse;
import org.openhab.binding.freebox.internal.api.model.SambaConfig;
import org.openhab.binding.freebox.internal.api.model.SambaConfigResponse;
import org.openhab.binding.freebox.internal.api.model.SystemConfigResponse;
import org.openhab.binding.freebox.internal.api.model.UPnPAVConfig;
import org.openhab.binding.freebox.internal.api.model.UPnPAVConfigResponse;
import org.openhab.binding.freebox.internal.api.model.VirtualMachineResponse;
import org.openhab.binding.freebox.internal.api.model.VirtualMachinesResponse;
import org.openhab.binding.freebox.internal.api.model.WifiConfig;
import org.openhab.binding.freebox.internal.api.model.WifiConfigResponse;
import org.openhab.binding.freebox.internal.api.model.XdslStatusResponse;
import org.osgi.framework.Bundle;

/**
 * The {@link APIRequests} defines all the action classes that can be used
 * when interaction with Freebox OS API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class APIRequests {

    @RequestAnnotation(relativeUrl = "api_version", responseClass = DiscoveryResponse.class)
    public static class checkAPI extends APIAction {
    }

    @RequestAnnotation(relativeUrl = "phone/?_dc=1415032391207", responseClass = PhoneStatusResponse.class)
    public static class PhoneStatus extends APIAction {
    }

    @RequestAnnotation(relativeUrl = "phone/config/?_dc=1415032391207", responseClass = PhoneConfigResponse.class)
    public static class PhoneConfig extends APIAction {
    }

    @RequestAnnotation(relativeUrl = "phone/", method = "POST")
    public static class RingPhone extends APIAction {
        public RingPhone(boolean startIt) {
            super(String.format("fxs_ring_%s", (startIt ? "start" : "stop")));
        }
    }

    @RequestAnnotation(relativeUrl = "call/log/?", responseClass = CallEntriesResponse.class)
    public static class CallEntries extends APIAction {
        public CallEntries(long lastCallTimestamp) {
            super(String.format("_dc=%d", lastCallTimestamp));
        }
    }

    @RequestAnnotation(relativeUrl = "lan/browser/pub/", responseClass = LanHostResponse.class)
    public static class LanHost extends APIAction {
        public LanHost(String mac) {
            super(String.format("ether-%s", mac));
        }
    }

    @RequestAnnotation(relativeUrl = "lan/wol/pub/", method = "POST")
    public static class LanHostWOL extends APIAction {
        public LanHostWOL(String mac) {
            super(new LanHostWOLData(mac));
        }
    }

    @RequestAnnotation(relativeUrl = "vm/", responseClass = VirtualMachineResponse.class)
    public static class VirtualMachine extends APIAction {
        public VirtualMachine(int vmId) {
            super(String.format("%d", vmId));
        }
    }

    @RequestAnnotation(relativeUrl = "vm/", method = "POST")
    public static class VirtualMachineAction extends APIAction {
        public VirtualMachineAction(int vmId, boolean startIt) {
            super(String.format("%d/%s", vmId, startIt ? "start" : "powerbutton"));
        }
    }

    @RequestAnnotation(relativeUrl = "airmedia/config/", responseClass = AirMediaConfigResponse.class)
    public static class GetAirMediaConfig extends APIAction {
    }

    @RequestAnnotation(relativeUrl = "airmedia/config/", responseClass = AirMediaConfigResponse.class, method = "PUT")
    public static class SetAirMediaConfig extends APIAction {
        public SetAirMediaConfig(boolean enable) {
            super(new AirMediaConfig(enable));
        }
    }

    @RequestAnnotation(relativeUrl = "airmedia/receivers/", method = "POST")
    public static class AirMediaAction extends APIAction {
        public AirMediaAction(String playerName, String password, MediaAction action, MediaType type) {
            super(playerName, new AirMediaActionData(password, action, type));
        }

        public AirMediaAction(String playerName, String password, MediaAction action, MediaType type, String url) {
            super(playerName, new AirMediaActionData(password, action, type, url));
        }
    }

    @RequestAnnotation(relativeUrl = "system/reboot/", method = "POST")
    public static class Reboot extends APIAction {
    }

    @RequestAnnotation(relativeUrl = "airmedia/receivers/", responseClass = AirMediaReceiversResponse.class)
    public static class AirMediaReceivers extends APIAction {
    }

    @RequestAnnotation(relativeUrl = "system/", responseClass = SystemConfigResponse.class)
    public static class SystemConfig extends APIAction {
    }

    @RequestAnnotation(relativeUrl = "connection/", responseClass = ConnectionStatusResponse.class)
    public static class ConnectionStatus extends APIAction {
    }

    @RequestAnnotation(relativeUrl = "lan/config/", responseClass = LanConfigResponse.class)
    public static class LanConfig extends APIAction {
    }

    @RequestAnnotation(relativeUrl = "connection/ftth/", responseClass = FtthStatusResponse.class)
    public static class FtthStatus extends APIAction {
    }

    @RequestAnnotation(relativeUrl = "connection/xdsl/", responseClass = XdslStatusResponse.class)
    public static class XdslStatus extends APIAction {
    }

    @RequestAnnotation(relativeUrl = "upnpav/config/", responseClass = UPnPAVConfigResponse.class)
    public static class GetUPnPAVConfig extends APIAction {
    }

    @RequestAnnotation(relativeUrl = "upnpnav/config/", responseClass = UPnPAVConfigResponse.class, method = "PUT")
    public static class SetUPnPAVConfig extends APIAction {
        public SetUPnPAVConfig(UPnPAVConfig config) {
            super(config);
        }
    }

    @RequestAnnotation(relativeUrl = "netshare/samba/", responseClass = SambaConfigResponse.class)
    public static class GetSambaConfig extends APIAction {
    }

    @RequestAnnotation(relativeUrl = "netshare/samba/", responseClass = SambaConfigResponse.class, method = "PUT")
    public static class SetSambaConfig extends APIAction {
        public SetSambaConfig(SambaConfig config) {
            super(config);
        }
    }

    @RequestAnnotation(relativeUrl = "lcd/config/", responseClass = LcdConfigResponse.class)
    public static class GetLcdConfig extends APIAction {
    }

    @RequestAnnotation(relativeUrl = "lcd/config/", responseClass = LcdConfigResponse.class, method = "PUT")
    public static class SetLcdConfig extends APIAction {
        public SetLcdConfig(LcdConfig config) {
            super(config);
        }
    }

    @RequestAnnotation(relativeUrl = "ftp/config/", responseClass = FtpConfigResponse.class)
    public static class GetFtpConfig extends APIAction {
    }

    @RequestAnnotation(relativeUrl = "ftp/config/", responseClass = FtpConfigResponse.class, method = "PUT")
    public static class SetFtpConfig extends APIAction {
        public SetFtpConfig(FtpConfig config) {
            super(config);
        }
    }

    @RequestAnnotation(relativeUrl = "wifi/config/", responseClass = WifiConfigResponse.class)
    public static class GetWifiConfig extends APIAction {
    }

    @RequestAnnotation(relativeUrl = "wifi/config/", responseClass = WifiConfigResponse.class, method = "PUT")
    public static class SetWifiConfig extends APIAction {
        public SetWifiConfig(WifiConfig config) {
            super(config);
        }
    }

    @RequestAnnotation(relativeUrl = "lan/browser/interfaces/", responseClass = LanInterfacesResponse.class)
    public static class LanInterfaces extends APIAction {
    }

    @RequestAnnotation(relativeUrl = "lan/browser/", responseClass = LanHostsResponse.class)
    public static class LanHosts extends APIAction {
        public LanHosts(String lanName) {
            super(lanName);
        }
    }

    @RequestAnnotation(relativeUrl = "vm/", responseClass = VirtualMachinesResponse.class)
    public static class VirtualMachines extends APIAction {
    }

    @RequestAnnotation(relativeUrl = "login/", maxRetries = 0, responseClass = LoginResponse.class)
    public static class Login extends APIAction {
    }

    @RequestAnnotation(relativeUrl = "login/logout/", maxRetries = 0, method = "POST")
    public static class Logout extends APIAction {
    }

    @RequestAnnotation(relativeUrl = "login/session/", responseClass = OpenSessionResponse.class, maxRetries = 0, method = "POST")
    public static class OpenSession extends APIAction {
        public OpenSession(String appId, String appToken, String challenge)
                throws InvalidKeyException, NoSuchAlgorithmException {
            super(new OpenSessionData(appId, appToken, challenge));
        }
    }

    @RequestAnnotation(relativeUrl = "login/authorize/", responseClass = AuthorizationStatusResponse.class, maxRetries = 0, endsWithSlash = false)
    public static class AuthorizationStatus extends APIAction {
        public AuthorizationStatus(int trackId) {
            super(String.format("%d", trackId));
        }
    }

    @RequestAnnotation(relativeUrl = "login/authorize/", responseClass = AuthorizeResponse.class, maxRetries = 0, method = "POST")
    public static class Authorize extends APIAction {
        public Authorize(String appId, Bundle bundle) {
            super(new AuthorizeData(appId, bundle));
        }
    }
}
