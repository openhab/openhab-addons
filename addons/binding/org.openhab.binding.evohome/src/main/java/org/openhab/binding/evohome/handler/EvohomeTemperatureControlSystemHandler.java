package org.openhab.binding.evohome.handler;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.evohome.EvohomeBindingConstants;
import org.openhab.binding.evohome.internal.api.EvohomeApiClient;
import org.openhab.binding.evohome.internal.api.models.ControlSystem;

public class EvohomeTemperatureControlSystemHandler extends BaseEvohomeHandler {
    private String controlSystemName;
    private int controlSystemId;

    public EvohomeTemperatureControlSystemHandler(Thing thing) {
        super(thing);

        Map<String, String> props = thing.getProperties();
        controlSystemId   = Integer.parseInt(props.get(EvohomeBindingConstants.DEVICE_ID));
        controlSystemName = props.get(EvohomeBindingConstants.DEVICE_NAME);
    }

    @Override
    public void initialize() {
       updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        EvohomeGatewayHandler gateway = (EvohomeGatewayHandler)getBridge().getHandler();
        EvohomeApiClient client = gateway.getApiClient();
        ControlSystem controlSystem = client.getControlSystem(controlSystemId);

        if ((controlSystem == null) || (getBridge().getStatus() == ThingStatus.OFFLINE)) {
            updateStatus(ThingStatus.OFFLINE);
        } else {
            if (command == RefreshType.REFRESH) {
                //TODO can probably go if we implement this in the Bridge
                update(client);
            } else  if (channelUID.getId().equals(EvohomeBindingConstants.SYSTEM_MODE_CHANNEL)) {
                controlSystem.setMode(command.toString());
            }
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void update(EvohomeApiClient client) {
        ControlSystem controlSystem = client.getControlSystem(controlSystemId);

        if (controlSystem != null) {
            String mode = controlSystem.getCurrentMode();
            updateState(EvohomeBindingConstants.SYSTEM_MODE_CHANNEL, new StringType(mode));
        }
    }

}
