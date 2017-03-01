package org.openhab.binding.elkm1.internal.elk.message;

import java.nio.charset.Charset;

import org.openhab.binding.elkm1.internal.elk.ElkCommand;
import org.openhab.binding.elkm1.internal.elk.ElkMessage;
import org.openhab.binding.elkm1.internal.elk.ElkZoneDefinition;

/**
 * The zone definition reply from the elk.
 *
 * @author David Bennett - Initial Contribution
 *
 */
public class ZoneDefinitionReply extends ElkMessage {
    private ElkZoneDefinition definitions[];

    public ZoneDefinitionReply(String incomingData) {
        super(ElkCommand.ZoneDefinitionReply);
        definitions = new ElkZoneDefinition[208];
        byte data[] = incomingData.getBytes(Charset.forName("US_ASCII"));
        for (int i = 0; i < 208; i++) {
            int def = data[i] - 0x30;
            definitions[i] = ElkZoneDefinition.fromInt(def);
        }
    }

    /** Definitions for all the zones. */
    public ElkZoneDefinition[] getDefinitions() {
        return definitions;
    }

    @Override
    protected String getData() {
        // TODO Auto-generated method stub
        return null;
    }

}
