package org.openhab.binding.surepetcare.internal.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SurePetcareDeviceCurfewListTest {

    @Test
    public void testGet() {
        SurePetcareDeviceCurfewList orig = new SurePetcareDeviceCurfewList();
        orig.add(new SurePetcareDeviceCurfew());
        orig.add(new SurePetcareDeviceCurfew(true, "12:00", "13:00"));

        assertEquals(orig.size(), 2);
        assertEquals(orig.get(0).enabled, false);
        assertEquals(orig.get(1).enabled, true);
        assertEquals(orig.size(), 2);

        assertEquals(orig.get(2).enabled, false);
        assertEquals(orig.size(), 3);

        assertEquals(orig.get(3).enabled, false);
        assertEquals(orig.size(), 4);

        assertEquals(orig.get(9).enabled, false);
        assertEquals(orig.size(), 10);
    }

    @Test
    public void testOrder() {
        SurePetcareDeviceCurfewList orig = new SurePetcareDeviceCurfewList();
        orig.add(new SurePetcareDeviceCurfew());
        orig.add(new SurePetcareDeviceCurfew(true, "12:00", "13:00"));
        orig.add(new SurePetcareDeviceCurfew(true, "19:00", "20:00"));

        assertEquals(orig.size(), 3);
        assertEquals(orig.get(0).enabled, false);
        assertEquals(orig.get(1).enabled, true);
        assertEquals(orig.get(2).enabled, true);

        SurePetcareDeviceCurfewList ordered = orig.order();
        assertEquals(ordered.size(), 4);

        assertEquals(ordered.get(0).enabled, true);
        assertEquals(ordered.get(0).lockTime, "12:00");
        assertEquals(ordered.get(1).enabled, true);
        assertEquals(ordered.get(2).enabled, false);
        assertEquals(ordered.get(3).enabled, false);
    }

    @Test
    public void testCompact() {
        SurePetcareDeviceCurfewList orig = new SurePetcareDeviceCurfewList();
        orig.add(new SurePetcareDeviceCurfew());
        orig.add(new SurePetcareDeviceCurfew(true, "12:00", "13:00"));
        orig.add(new SurePetcareDeviceCurfew(true, "19:00", "20:00"));
        orig.add(new SurePetcareDeviceCurfew());

        assertEquals(orig.size(), 4);
        assertEquals(orig.get(0).enabled, false);
        assertEquals(orig.get(1).enabled, true);
        assertEquals(orig.get(2).enabled, true);
        assertEquals(orig.get(3).enabled, false);

        SurePetcareDeviceCurfewList compact = orig.compact();
        assertEquals(compact.size(), 2);

        assertEquals(compact.get(0).enabled, true);
        assertEquals(compact.get(0).lockTime, "12:00");
        assertEquals(compact.get(1).enabled, true);
        assertEquals(compact.get(1).lockTime, "19:00");
    }

}
