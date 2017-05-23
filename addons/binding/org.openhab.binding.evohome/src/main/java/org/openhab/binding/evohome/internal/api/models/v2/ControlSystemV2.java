package org.openhab.binding.evohome.internal.api.models.v2;

import java.util.ArrayList;

import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.evohome.internal.api.EvohomeApiConstants;
import org.openhab.binding.evohome.internal.api.models.BaseControlSystem;
import org.openhab.binding.evohome.internal.api.models.v2.request.Mode;
import org.openhab.binding.evohome.internal.api.models.v2.response.TemperatureControlSystem;
import org.openhab.binding.evohome.internal.api.models.v2.response.TemperatureControlSystemStatus;

public class ControlSystemV2 extends BaseControlSystem {
    private ApiAccess apiAccess = null;
    private TemperatureControlSystem system;
    private TemperatureControlSystemStatus status;

    public ControlSystemV2(ApiAccess apiAccess, TemperatureControlSystem system, TemperatureControlSystemStatus status) {
        super(system.SystemId, system.ModelType);
        this.apiAccess = apiAccess;
        this.system    = system;
        this.status    = status;
    }

    @Override
    public String[] getModes() {
        ArrayList<String> modes = new ArrayList<String>();

        for (org.openhab.binding.evohome.internal.api.models.v2.response.Mode mode : system.AllowedSystemModes) {
            modes.add(mode.SystemMode);
        }

        return modes.toArray(new String[modes.size()]);
    }

    @Override
    public String getCurrentMode() {
        if (status != null) {
            return status.Mode.Mode;
        }
        return null;
    }

    @Override
    public TemperatureControlSystem getHeatingZones() {
        return this.system;
    }

    @Override
    public void setMode(String mode) {
        String url = EvohomeApiConstants.URL_V2_BASE + EvohomeApiConstants.URL_V2_MODE;
        url = String.format(url, getId());
        apiAccess.doAuthenticatedRequest(HttpMethod.PUT, url, null, new Mode(mode), null);
    }

}
