package org.openhab.binding.mikrotik.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.joda.time.DateTime;

import java.util.Map;

@NonNullByDefault
public class RouterosL2TPSrvInterface extends RouterosInterfaceBase {
    public RouterosL2TPSrvInterface(Map<String, String> props) {
        super(props);
    }
    @Override
    protected RouterosInterfaceType[] getDesignedTypes() {
        return new RouterosInterfaceType[]{RouterosInterfaceType.L2TP_SERVER};
    }

    public String getUptime(){ return propMap.get("uptime"); } //TODO monitor once
    public DateTime calculateUptimeStart(){
        return DateTime.now().minusHours(2); //TODO
    }



}
