package org.openhab.core.compat1x.internal;

import static org.junit.Assert.*;

import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.items.DateTimeItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Test;
import org.openhab.core.items.Item;

public class ItemMapperTest {


    @Test
    public void testESH2OHItemMapping() {
        Item item = null;
        
        item = ItemMapper.mapToOpenHABItem(new StringItem("test1"));
        assertEquals(org.openhab.core.library.items.StringItem.class, item.getClass());
        assertEquals("test1", item.getName());

        item = ItemMapper.mapToOpenHABItem(new NumberItem("test2"));
        assertEquals(org.openhab.core.library.items.NumberItem.class, item.getClass());
        assertEquals("test2", item.getName());

        item = ItemMapper.mapToOpenHABItem(new DimmerItem("test3"));
        assertEquals(org.openhab.core.library.items.DimmerItem.class, item.getClass());
        assertEquals("test3", item.getName());

        item = ItemMapper.mapToOpenHABItem(new RollershutterItem("test4"));
        assertEquals(org.openhab.core.library.items.RollershutterItem.class, item.getClass());
        assertEquals("test4", item.getName());

        item = ItemMapper.mapToOpenHABItem(new ColorItem("test5"));
        assertEquals(org.openhab.core.library.items.ColorItem.class, item.getClass());
        assertEquals("test5", item.getName());

        item = ItemMapper.mapToOpenHABItem(new ContactItem("test6"));
        assertEquals(org.openhab.core.library.items.ContactItem.class, item.getClass());
        assertEquals("test6", item.getName());

        item = ItemMapper.mapToOpenHABItem(new DateTimeItem("test7"));
        assertEquals(org.openhab.core.library.items.DateTimeItem.class, item.getClass());
        assertEquals("test7", item.getName());

        item = ItemMapper.mapToOpenHABItem(new SwitchItem("test8"));
        assertEquals(org.openhab.core.library.items.SwitchItem.class, item.getClass());
        assertEquals("test8", item.getName());
    }
    
    @Test
    public void testGroupItemMapping() {
        org.openhab.core.items.GroupItem group = (org.openhab.core.items.GroupItem) ItemMapper.mapToOpenHABItem(new GroupItem("group1"));
        assertEquals("group1", group.getName());
        assertNull(group.getBaseItem());
        
        group = (org.openhab.core.items.GroupItem) ItemMapper.mapToOpenHABItem(new GroupItem("group2", new NumberItem("baseItem")));
        assertEquals("group2", group.getName());
        assertEquals(org.openhab.core.library.items.NumberItem.class, group.getBaseItem().getClass());

        GroupItem eshGroup = new GroupItem("group3");
        for(int i = 1; i <= 10; i++) {
            eshGroup.addMember(new StringItem(Integer.toString(i)));
        }
        group = (org.openhab.core.items.GroupItem) ItemMapper.mapToOpenHABItem(eshGroup);
        assertEquals("group3", group.getName());
        assertEquals(10, group.getMembers().size());
        assertEquals(org.openhab.core.library.items.StringItem.class, group.getMembers().iterator().next().getClass());        
    }
    
    @Test
    public void testItemStateMapping() {
        StringItem item = new StringItem("test");
        item.setState(UnDefType.NULL);        
        assertEquals(org.openhab.core.types.UnDefType.NULL, ItemMapper.mapToOpenHABItem(item).getState());
        item.setState(UnDefType.UNDEF);        
        assertEquals(org.openhab.core.types.UnDefType.UNDEF, ItemMapper.mapToOpenHABItem(item).getState());
        item.setState(new StringType("ABC"));        
        assertEquals(new org.openhab.core.library.types.StringType("ABC"), ItemMapper.mapToOpenHABItem(item).getState());
    }

}
