package org.openhab.binding.edimax.handler;

import java.io.IOException;
import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.edimax.EdimaxBindingConstants;
import org.openhab.binding.edimax.internal.commands.GetCurrent;
import org.openhab.binding.edimax.internal.commands.GetPower;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Edimax2101Handler extends EdimaxHandler {

    private Logger logger = LoggerFactory.getLogger(Edimax2101Handler.class);

    public Edimax2101Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        try {
            if (channelUID.getId().equals(EdimaxBindingConstants.CURRENT)) {
                if (command instanceof RefreshType) {
                    final DecimalType current = new DecimalType(getCurrent());
                    logger.debug("Current: " + current);
                }
            }
            if (channelUID.getId().equals(EdimaxBindingConstants.POWER)) {
                if (command instanceof RefreshType) {
                    final DecimalType power = new DecimalType(getPower());
                    logger.debug("Current: " + power);
                }
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }

    }

    /**
     * Returns the current.
     *
     * @return
     * @throws IOException
     */
    public BigDecimal getCurrent() throws IOException {
        final GetCurrent getC = new GetCurrent();
        return getC.executeCommand(ci);
    }

    /**
     * Gets the actual power.
     *
     * @return
     * @throws IOExceptionif
     */
    public BigDecimal getPower() throws IOException {
        final GetPower getC = new GetPower();
        return getC.executeCommand(ci);
    }

}
