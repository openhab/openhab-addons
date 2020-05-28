/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.blinds.action;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.model.script.ScriptServiceUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Markus Pfleger - Initial contribution
 */
public class BlindsTest {
    private static org.eclipse.smarthome.core.library.items.RollershutterItem rollershutterUnregistered = new org.eclipse.smarthome.core.library.items.RollershutterItem(
            "rollershutterUnregistered");

    private static org.eclipse.smarthome.core.library.items.RollershutterItem rollershutter1 = new org.eclipse.smarthome.core.library.items.RollershutterItem(
            "rollershutter1");
    private static org.eclipse.smarthome.core.library.items.DimmerItem slat1 = new org.eclipse.smarthome.core.library.items.DimmerItem(
            "slat1");

    private static org.eclipse.smarthome.core.library.items.RollershutterItem rollershutter2 = new org.eclipse.smarthome.core.library.items.RollershutterItem(
            "rollershutter2");
    private static org.eclipse.smarthome.core.library.items.DimmerItem slat2 = new org.eclipse.smarthome.core.library.items.DimmerItem(
            "slat2");
    private static org.eclipse.smarthome.core.library.items.ContactItem windowContact2 = new org.eclipse.smarthome.core.library.items.ContactItem(
            "window2");
    private static org.eclipse.smarthome.core.library.items.NumberItem temperature2 = new org.eclipse.smarthome.core.library.items.NumberItem(
            "temperature2");

    private static org.eclipse.smarthome.core.library.items.RollershutterItem rollershutter3 = new org.eclipse.smarthome.core.library.items.RollershutterItem(
            "rollershutter3");
    private static org.eclipse.smarthome.core.library.items.DimmerItem slat3 = new org.eclipse.smarthome.core.library.items.DimmerItem(
            "slat3");
    private static org.eclipse.smarthome.core.library.items.ContactItem windowContact3 = new org.eclipse.smarthome.core.library.items.ContactItem(
            "window3");

    private static org.eclipse.smarthome.core.items.GroupItem group1 = new org.eclipse.smarthome.core.items.GroupItem(
            "group1");

    private static RollershutterItem OrollershutterUnregistered = new RollershutterItem("rollershutterUnregistered");

    private static RollershutterItem Orollershutter1 = new RollershutterItem("rollershutter1");
    private static DimmerItem Oslat1 = new DimmerItem("slat1");

    private static RollershutterItem Orollershutter2 = new RollershutterItem("rollershutter2");
    private static DimmerItem Oslat2 = new DimmerItem("slat2");
    private static ContactItem OwindowContact2 = new ContactItem("window2");
    private static NumberItem Otemperature2 = new NumberItem("temperature2");

    private static RollershutterItem Orollershutter3 = new RollershutterItem("rollershutter3");
    private static DimmerItem Oslat3 = new DimmerItem("slat3");
    private static ContactItem OwindowContact3 = new ContactItem("window3");

    private static GroupItem Ogroup1 = new GroupItem("group1");

    private static Map<String, Item> itemMap = new HashMap<String, Item>();

    private BlindActions actions;

    @BeforeClass
    public static void setUpClass() throws Exception {
        group1.addMember(rollershutter1);
        group1.addMember(rollershutter2);

        itemMap.put(rollershutter1.getName(), Orollershutter1);
        itemMap.put(rollershutter2.getName(), Orollershutter2);
        itemMap.put(rollershutter3.getName(), Orollershutter3);
        itemMap.put(rollershutterUnregistered.getName(), OrollershutterUnregistered);
        itemMap.put(group1.getName(), Ogroup1);

        itemMap.put(slat1.getName(), Oslat1);
        itemMap.put(slat2.getName(), Oslat2);
        itemMap.put(slat3.getName(), Oslat3);

        itemMap.put(windowContact2.getName(), OwindowContact2);
        itemMap.put(windowContact3.getName(), OwindowContact3);

        itemMap.put(temperature2.getName(), Otemperature2);

        ItemRegistry itemRegistry = Mockito.mock(ItemRegistry.class);
        new ScriptServiceUtil(itemRegistry, null, null, null);
        Mockito.when(itemRegistry.getItem(ArgumentMatchers.anyString())).thenAnswer(new Answer<Item>() {

            @Override
            public Item answer(InvocationOnMock invocation) throws Throwable {
                String itemName = (String) invocation.getArguments()[0];
                return itemMap.get(itemName);
            }
        });

    }

    @Before
    public void setUp() throws Exception {
        actions = new BlindActions();
        actions.activate();

        // service.setEventPublisher(Mockito.mock(EventPublisher.class));
        // service.setItemRegistry();

        actions.clearBlindItems();
    }

    @After
    public void tearDown() {
        actions.deactivate();
    }

    @Test
    public void testItemCreation() {
        Assert.assertEquals(1, actions.createBlindItem(rollershutter1, slat1));
        Assert.assertEquals(1, actions.getBlinds().size());

        Assert.assertEquals(2, actions.createBlindItem(rollershutter2, slat2, windowContact2, temperature2));
        Assert.assertEquals(2, actions.getBlinds().size());

        Assert.assertEquals(1, actions.createBlindItem(rollershutter1, slat1));
        Assert.assertEquals(2, actions.getBlinds().size());

        Assert.assertEquals(3, actions.createBlindItem(rollershutter3, slat3, windowContact3, temperature2));
        Assert.assertEquals(3, actions.getBlinds().size());

        Assert.assertTrue(actions.getBlinds().contains(1));
        Assert.assertTrue(actions.getBlinds(rollershutter1).contains(1));
        Assert.assertTrue(actions.getBlinds(rollershutterUnregistered).isEmpty());
    }

    @Test
    public void testGetAutomaticBlinds() {
        int blind1 = actions.createBlindItem(rollershutter1, slat1);

        // test automatic raffstores
        Assert.assertFalse(actions.getAutomaticBlinds().contains(blind1));
        Assert.assertFalse(actions.getAutomaticBlinds(rollershutter1).contains(blind1));
        actions.startBlindAutomaticProgram(blind1);
        Assert.assertTrue(actions.getAutomaticBlinds().contains(blind1));
        Assert.assertTrue(actions.getAutomaticBlinds(rollershutter1).contains(blind1));
        actions.stopBlindAutomaticProgram(blind1);
        Assert.assertFalse(actions.getAutomaticBlinds().contains(blind1));
        Assert.assertFalse(actions.getAutomaticBlinds(rollershutter1).contains(blind1));
    }

    @Test
    public void testGetAutomaticBlindsGroupItem() {
        int blind1 = actions.createBlindItem(rollershutter1, slat1);
        int blind2 = actions.createBlindItem(rollershutter2, slat2);
        int blind3 = actions.createBlindItem(rollershutter3, slat3);

        actions.startBlindAutomaticProgram(blind1);
        actions.startBlindAutomaticProgram(blind2);
        actions.startBlindAutomaticProgram(blind3);

        Assert.assertEquals(2, actions.getAutomaticBlinds(group1).size());
        Assert.assertTrue(actions.getAutomaticBlinds(group1).contains(blind1));
        Assert.assertTrue(actions.getAutomaticBlinds(group1).contains(blind2));
        Assert.assertFalse(actions.getAutomaticBlinds(group1).contains(blind3));
    }

    @Test
    public void testGetBlinds() {
        int blind1 = actions.createBlindItem(rollershutter1, slat1);
        int blind2 = actions.createBlindItem(rollershutter2, slat2, windowContact2, temperature2);

        // test fetch all blinds
        Set<Integer> allBlinds = actions.getBlinds((org.eclipse.smarthome.core.items.Item) null);
        Assert.assertEquals(2, allBlinds.size());
        Assert.assertTrue(allBlinds.contains(blind1));
        Assert.assertTrue(allBlinds.contains(blind2));
    }

    public void testTemperatureRange() {
        int blind1 = actions.createBlindItem(rollershutter1, slat1);
        int blind2 = actions.createBlindItem(rollershutter2, slat2, windowContact2, temperature2);
        int blind3 = actions.createBlindItem(rollershutter3, slat3, windowContact3, temperature2);

        Set<Integer> allBlinds = actions.getBlinds((org.eclipse.smarthome.core.items.Item) null);
        Assert.assertEquals(3, allBlinds.size());

        // test temperature range
        Assert.assertTrue(actions.getBlindsAboveTemperatureRange(allBlinds).isEmpty());
        Assert.assertTrue(actions.getBlindsBelowTemperatureRange(allBlinds).isEmpty());

        actions.setBlindTemperatureRange(blind2, 18, 22);
        Assert.assertTrue("Should still be empty as the number item has no value set",
                actions.getBlindsAboveTemperatureRange(allBlinds).isEmpty());
        Assert.assertTrue("Should still be empty as the number item has no value set",
                actions.getBlindsBelowTemperatureRange(allBlinds).isEmpty());

        Otemperature2.setState(new DecimalType(15));
        Assert.assertTrue(actions.getBlindsAboveTemperatureRange(allBlinds).isEmpty());
        Assert.assertTrue(actions.getBlindsBelowTemperatureRange(allBlinds).contains(blind2));
        Assert.assertTrue(actions.getBlindsNearLowerTemperatureRange(allBlinds, 0.5).isEmpty());

        Otemperature2.setState(new DecimalType(18.2));
        Assert.assertTrue(actions.getBlindsAboveTemperatureRange(allBlinds).isEmpty());
        Assert.assertTrue(actions.getBlindsBelowTemperatureRange(allBlinds).isEmpty());
        Assert.assertTrue(actions.getBlindsNearLowerTemperatureRange(allBlinds, 0.1).isEmpty());
        Assert.assertTrue(actions.getBlindsNearLowerTemperatureRange(allBlinds, 0.5).contains(blind2));

        Otemperature2.setState(new DecimalType(20));
        Assert.assertTrue(actions.getBlindsAboveTemperatureRange(allBlinds).isEmpty());
        Assert.assertTrue(actions.getBlindsBelowTemperatureRange(allBlinds).isEmpty());

        Otemperature2.setState(new DecimalType(21.7));
        Assert.assertTrue(actions.getBlindsAboveTemperatureRange(allBlinds).isEmpty());
        Assert.assertTrue(actions.getBlindsBelowTemperatureRange(allBlinds).isEmpty());
        Assert.assertTrue(actions.getBlindsNearUpperTemperatureRange(allBlinds, 0.1).isEmpty());
        Assert.assertTrue(actions.getBlindsNearUpperTemperatureRange(allBlinds, 0.5).contains(blind2));

        Otemperature2.setState(new DecimalType(23));
        Assert.assertTrue(actions.getBlindsBelowTemperatureRange(allBlinds).isEmpty());
        Assert.assertTrue(actions.getBlindsAboveTemperatureRange(allBlinds).contains(blind2));
        Assert.assertTrue(actions.getBlindsNearUpperTemperatureRange(allBlinds, 0.5).isEmpty());

        actions.setBlindTemperatureRange(blind1, 10, 18);
        actions.setBlindTemperatureRange(blind3, 23, 30);

        Otemperature2.setState(new DecimalType(20));

        Assert.assertEquals(1, actions.getBlindsBelowTemperatureRange(allBlinds).size());
        Assert.assertTrue(actions.getBlindsBelowTemperatureRange(allBlinds).contains(blind1));

        Assert.assertEquals(1, actions.getBlindsAboveTemperatureRange(allBlinds).size());
        Assert.assertTrue(actions.getBlindsAboveTemperatureRange(allBlinds).contains(blind3));

        Otemperature2.setState(new DecimalType(15));

        actions.setBlindTemperatureRange(blind1, 20, 25);
        actions.setBlindTemperatureRange(blind2, 20, 25);
        actions.setBlindTemperatureRange(blind3, 20, 25);

        Assert.assertEquals(3, actions.getBlindsBelowTemperatureRange(allBlinds).size());
        Assert.assertEquals(2, actions.getBlindsBelowTemperatureRange(group1).size());
        Assert.assertTrue(actions.getBlindsBelowTemperatureRange(group1).contains(blind1));
        Assert.assertTrue(actions.getBlindsBelowTemperatureRange(group1).contains(blind2));

        Otemperature2.setState(new DecimalType(30));

        Assert.assertEquals(3, actions.getBlindsAboveTemperatureRange(allBlinds).size());
        Assert.assertEquals(2, actions.getBlindsAboveTemperatureRange(group1).size());
        Assert.assertTrue(actions.getBlindsAboveTemperatureRange(group1).contains(blind1));
        Assert.assertTrue(actions.getBlindsAboveTemperatureRange(group1).contains(blind2));
    }

    @Test
    public void testSunRange() {
        int blind1 = actions.createBlindItem(rollershutter1, slat1);
        int blind2 = actions.createBlindItem(rollershutter2, slat2, windowContact2, temperature2);
        int blind3 = actions.createBlindItem(rollershutter3, slat3, windowContact3, temperature2);

        Set<Integer> allBlinds = actions.getBlinds((org.eclipse.smarthome.core.items.Item) null);
        Assert.assertTrue(actions.getBlindsInSunRange(allBlinds, 90, 30).isEmpty());
        Assert.assertTrue(actions.getBlindsNearSunRange(allBlinds, 90, 45, 30, 10).isEmpty());

        actions.setBlindSunRange(blind1, 80, 120);
        actions.setBlindSunRange(blind2, 80, 120, 35, 45);
        actions.setBlindSunRange(blind3, 70, 80);

        Assert.assertEquals(1, actions.getBlindsInSunRange(allBlinds, 90, 30).size());
        Assert.assertTrue(actions.getBlindsInSunRange(allBlinds, 90, 30).contains(blind1));

        Assert.assertEquals(1, actions.getBlindsInSunRange(group1, 90, 30).size());
        Assert.assertTrue(actions.getBlindsInSunRange(group1, 90, 30).contains(blind1));

        Assert.assertEquals(3, actions.getBlindsNearSunRange(allBlinds, 90, 20, 30, 10).size());
        Assert.assertTrue(actions.getBlindsNearSunRange(allBlinds, 90, 20, 30, 10).contains(blind1));
        Assert.assertTrue(actions.getBlindsNearSunRange(allBlinds, 90, 20, 30, 10).contains(blind2));
        Assert.assertTrue(actions.getBlindsNearSunRange(allBlinds, 90, 20, 30, 10).contains(blind3));

        Assert.assertEquals(2, actions.getBlindsNearSunRange(group1, 90, 20, 30, 10).size());
        Assert.assertTrue(actions.getBlindsNearSunRange(group1, 90, 20, 30, 10).contains(blind1));
        Assert.assertTrue(actions.getBlindsNearSunRange(group1, 90, 20, 30, 10).contains(blind2));
        Assert.assertFalse(actions.getBlindsNearSunRange(group1, 90, 20, 30, 10).contains(blind3));
    }

    // @Test
    // public void testMoveBlindsTo() {
    // int blind1 = Blinds.createBlindItem(rollershutter1, slat1);
    // int blind2 = Blinds.createBlindItem(rollershutter2, slat2, windowContact2, temperature2);
    // int blind3 = Blinds.createBlindItem(rollershutter3, slat3, windowContact3, temperature2);
    //
    // Blinds.moveBlindTo(blind1, 100, 50, OpenClosedType.CLOSED);
    // }
}
