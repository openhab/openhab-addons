package org.openhab.binding.mikrotik.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.joda.time.DateTime;

import java.util.Map;

@NonNullByDefault
public class RouterosPPPoECliInterface extends RouterosInterfaceBase {
    public RouterosPPPoECliInterface(Map<String, String> props) {
        super(props);
    }
    @Override
    protected RouterosInterfaceType[] getDesignedTypes() {
        return new RouterosInterfaceType[]{RouterosInterfaceType.PPPOE_CLIENT};
    }

    public String getUptime(){ return propMap.get("uptime"); } //TODO monitor once
    public DateTime calculateUptimeStart(){
        return DateTime.now().minusHours(2); //TODO
    }

}
