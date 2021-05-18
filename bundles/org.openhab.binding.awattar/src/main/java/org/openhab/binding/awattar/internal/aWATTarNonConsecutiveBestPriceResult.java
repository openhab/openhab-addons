package org.openhab.binding.awattar.internal;

import static org.openhab.binding.awattar.internal.aWATTarUtil.*;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class aWATTarNonConsecutiveBestPriceResult extends aWATTarBestPriceResult {

    private List<aWATTarPrice> members;
    private ZoneId zoneId;
    private boolean sorted = true;

    public aWATTarNonConsecutiveBestPriceResult(int size, ZoneId zoneId) {
        super();
        this.zoneId = zoneId;
        members = new ArrayList<aWATTarPrice>();
    }

    public void addMember(aWATTarPrice member) {
        sorted = false;
        members.add(member);
        updateStart(member.getStartTimestamp());
        updateEnd(member.getEndTimestamp());
    }

    @Override
    public boolean isActive() {
        return members.stream().anyMatch(x -> x.contains(Instant.now().toEpochMilli()));
    }

    public String toString() {
        return String.format("NonConsecutiveBestpriceResult with %s", members.toString());
    }

    private void sort() {
        if (!sorted) {
            members.sort(new Comparator<aWATTarPrice>() {
                @Override
                public int compare(aWATTarPrice o1, aWATTarPrice o2) {
                    return Long.compare(o1.getStartTimestamp(), o2.getStartTimestamp());
                }
            });
        }
    }

    public String getHours() {
        boolean second = false;
        sort();
        StringBuilder res = new StringBuilder();
        for (aWATTarPrice price : members) {
            if (second) {
                res.append(',');
            }
            res.append(getHourFrom(price.getStartTimestamp(), zoneId));
            second = true;
        }
        return res.toString();
    }
}
