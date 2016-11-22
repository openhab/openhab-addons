package org.openhab.binding.ivtheatpump.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.ivtheatpump.IVTHeatPumpBindingConstants;
import org.openhab.binding.ivtheatpump.internal.protocol.IVRConnection;
import org.openhab.binding.ivtheatpump.internal.protocol.IpIVRConnection;

public class IpIVTHeatPumpHandler extends IVTHeatPumpHandler {
    public IpIVTHeatPumpHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected IVRConnection createConnection() {
        String host = (String) getConfig().get(IVTHeatPumpBindingConstants.HOST_PARAMETER);
        Integer port = ((Number) getConfig().get(IVTHeatPumpBindingConstants.TCP_PORT_PARAMETER)).intValue();

        return new IpIVRConnection(host, port);
    }
}
