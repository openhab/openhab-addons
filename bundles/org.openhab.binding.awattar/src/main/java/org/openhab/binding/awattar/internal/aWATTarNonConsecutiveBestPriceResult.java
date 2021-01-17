package org.openhab.binding.awattar.internal;

import static org.openhab.binding.awattar.internal.aWATTarUtil.*;

import java.util.Comparator;
import java.util.Date;
import java.util.TreeSet;

public class aWATTarNonConsecutiveBestPriceResult extends aWATTarBestPriceResult {

    private TreeSet<aWATTarPrice> members;

    public aWATTarNonConsecutiveBestPriceResult(int size) {
        super();
        members = new TreeSet<>(new Comparator<aWATTarPrice>() {
            @Override
            public int compare(aWATTarPrice o1, aWATTarPrice o2) {
                return Long.compare(o1.getStartTimestamp(), o2.getStartTimestamp());
            }
        });
    }

    public void addMember(aWATTarPrice member) {
        members.add(member);
        updateStart(member.getStartTimestamp());
        updateEnd(member.getEndTimestamp());
    }

    @Override
    public boolean isActive() {
        return members.stream().anyMatch(x -> x.contains(new Date().getTime()));
    }

    public String toString() {
        return String.format("NonConsecutiveBestpriceResult with %s", members.toString());
    }

    public String getHours() {
        boolean second = false;
        StringBuilder res = new StringBuilder();
        for (aWATTarPrice price : members) {
            if (second) {
                res.append(',');
            }
            res.append(getHourFrom(price.getStartTimestamp()));
            second = true;
        }
        return res.toString();
    }
}
