package org.openhab.binding.elkm1.internal.elk.message;

import org.openhab.binding.elkm1.internal.elk.ElkCommand;
import org.openhab.binding.elkm1.internal.elk.ElkMessage;

/**
 * Requests all the zone definitions from the elk.
 *
 * @author David Bennett - Initial Contribution
 *
 */
public class ZoneDefinitionRequest extends ElkMessage {
    public ZoneDefinitionRequest() {
        super(ElkCommand.ZoneDefintionRequest);
    }

    @Override
    protected String getData() {
        return "";
    }
}
