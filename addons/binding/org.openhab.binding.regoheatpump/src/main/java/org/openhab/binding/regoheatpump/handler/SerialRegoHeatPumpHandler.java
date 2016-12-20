package org.openhab.binding.regoheatpump.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.regoheatpump.RegoHeatPumpBindingConstants;
import org.openhab.binding.regoheatpump.internal.protocol.RegoConnection;
import org.openhab.binding.regoheatpump.internal.protocol.SerialRegoConnection;

public class SerialRegoHeatPumpHandler extends RegoHeatPumpHandler {
    public SerialRegoHeatPumpHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected RegoConnection createConnection() {
        final String portName = (String) getConfig().get(RegoHeatPumpBindingConstants.PORT_NAME);
        return new SerialRegoConnection(portName, 19200);
    }
}
