package org.openhab.binding.isy.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.isy.IsyBindingConstants;
import org.openhab.binding.isy.config.IsyVariableConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsyVariableHandler extends AbtractIsyThingHandler implements IsyThingHandler {
    private static final Logger logger = LoggerFactory.getLogger(IsyVariableHandler.class);

    public IsyVariableHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO implement
        if (RefreshType.REFRESH.equals(command)) {
            logger.debug("Must implement refresh for variables");
        } else {
            if (command instanceof DecimalType) {
                IsyVariableConfiguration var_config = getThing().getConfiguration().as(IsyVariableConfiguration.class);
                getBridgeHandler().getInsteonClient().changeVariableState(var_config.type, var_config.id,
                        ((DecimalType) command).intValue());
            } else {
                logger.warn("Unsupported command for variable handleCommand");
            }
        }

    }

    @Override
    public void handleUpdate(Object... parameters) {
        String[] updateInfo = ((String) parameters[2]).split(" ");
        // logger.warn("Unhandled update");
        updateState(IsyBindingConstants.CHANNEL_VARIABLE_VALUE, new DecimalType(updateInfo[2]));
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
