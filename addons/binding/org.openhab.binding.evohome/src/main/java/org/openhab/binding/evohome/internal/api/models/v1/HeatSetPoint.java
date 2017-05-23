package org.openhab.binding.evohome.internal.api.models.v1;

import java.math.BigDecimal;
import java.util.Date;

public class HeatSetPoint {
    private BigDecimal value;
    private String status;
    private Date nextTime;

    @Override
    public String toString() {
        return "value[" + value + "] status[" + status + "] nextTime[" + nextTime + "]";
    }

    public Date getNextTime() {
        return nextTime;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getValue() {
        return value;
    }
}
