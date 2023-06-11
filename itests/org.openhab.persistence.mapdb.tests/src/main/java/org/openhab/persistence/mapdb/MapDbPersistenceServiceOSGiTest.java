/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.persistence.mapdb;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.items.GenericItem;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.test.java.JavaOSGiTest;
import org.openhab.core.types.State;
import org.openhab.persistence.mapdb.internal.MapDbPersistenceService;

/**
 *
 * @author Martin Kühl - Initial contribution
 */
public class MapDbPersistenceServiceOSGiTest extends JavaOSGiTest {
    private MapDbPersistenceService persistenceService;

    @BeforeEach
    public void setUp() {
        persistenceService = getService(QueryablePersistenceService.class, MapDbPersistenceService.class);
    }

    @AfterAll
    public static void tearDown() throws IOException {
        // clean up database files ...
        removeDirRecursive("userdata");
        removeDirRecursive("runtime");
    }

    private static void removeDirRecursive(final String dir) throws IOException {
        final Path path = Paths.get(dir);
        if (Files.exists(path)) {
            Files.walk(path).map(Path::toFile).sorted().forEach(File::delete);
        }
    }

    @Test
    public void storeShouldStoreTheItem() {
        String name = "switch1";
        String alias = "switch2";
        State state = OnOffType.ON;

        GenericItem item = new SwitchItem(name);
        item.setState(state);

        assertThat(persistenceService.getItemInfo(), not(hasItem(hasProperty("name", equalTo(name)))));

        persistenceService.store(item);

        assertThat(persistenceService.getItemInfo(), hasItem(hasProperty("name", equalTo(name))));

        persistenceService.store(item, alias);

        assertThat(persistenceService.getItemInfo(),
                hasItems(hasProperty("name", equalTo(name)), hasProperty("name", equalTo(alias))));
    }

    @Test
    public void queryShouldFindStoredItemsByName() {
        String name = "dimmer";
        State state = PercentType.HUNDRED;

        GenericItem item = new DimmerItem(name);
        item.setState(state);

        FilterCriteria filter = new FilterCriteria();
        filter.setItemName(name);

        assertThat(persistenceService.query(filter), is(emptyIterable()));

        persistenceService.store(item);

        assertThat(persistenceService.query(filter),
                contains(allOf(hasProperty("name", equalTo(name)), hasProperty("state", equalTo(state)))));
    }

    @Test
    public void queryShouldFindStoredItemsByAlias() {
        String name = "color";
        String alias = "alias";
        State state = HSBType.GREEN;

        GenericItem item = new ColorItem(name);
        item.setState(state);

        FilterCriteria filterByName = new FilterCriteria();
        filterByName.setItemName(name);

        FilterCriteria filterByAlias = new FilterCriteria();
        filterByAlias.setItemName(alias);

        assertThat(persistenceService.query(filterByName), is(emptyIterable()));
        assertThat(persistenceService.query(filterByAlias), is(emptyIterable()));

        persistenceService.store(item, alias);

        assertThat(persistenceService.query(filterByName), is(emptyIterable()));
        assertThat(persistenceService.query(filterByAlias),
                contains(allOf(hasProperty("name", equalTo(alias)), hasProperty("state", equalTo(state)))));
    }
}
