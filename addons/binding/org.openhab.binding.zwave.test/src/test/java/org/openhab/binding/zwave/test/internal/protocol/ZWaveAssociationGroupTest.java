package org.openhab.binding.zwave.test.internal.protocol;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.ZWaveAssociationGroup;

public class ZWaveAssociationGroupTest {
    @Test
    public void TestAssociationGroup() {
        ZWaveAssociationGroup group = new ZWaveAssociationGroup(1);

        group.addAssociation(1);
        assertEquals(1, group.getAssociationCnt());
        assertTrue(group.isAssociated(1, 0));

        group.addAssociation(1, 0);
        assertEquals(1, group.getAssociationCnt());

        group.addAssociation(3, 2);
        assertEquals(2, group.getAssociationCnt());
    }
}
