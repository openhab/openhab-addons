package org.openhab.binding.regoheatpump.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.regoheatpump.RegoHeatPumpBindingConstants;
import org.openhab.binding.regoheatpump.internal.protocol.RegoConnection;
import org.openhab.binding.regoheatpump.internal.protocol.IpRegoConnection;

public class IpRegoHeatPumpHandler extends RegoHeatPumpHandler {
    public IpRegoHeatPumpHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected RegoConnection createConnection() {
        String host = (String) getConfig().get(RegoHeatPumpBindingConstants.HOST_PARAMETER);
        Integer port = ((Number) getConfig().get(RegoHeatPumpBindingConstants.TCP_PORT_PARAMETER)).intValue();

        return new IpRegoConnection(host, port);
    }
}
