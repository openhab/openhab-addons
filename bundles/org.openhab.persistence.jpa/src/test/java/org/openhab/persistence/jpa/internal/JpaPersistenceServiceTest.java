/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.persistence.jpa.internal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.DecimalType;
import org.osgi.framework.BundleContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

/**
 * Tests for {@link JpaPersistenceService}.
 *
 * @author Cody Cutrer
 */
@NonNullByDefault
class JpaPersistenceServiceTest {

    @Test
    void storeDoesNotRollbackInactiveTransactionAfterCommitFailure() {
        EntityManagerFactory entityManagerFactory = mock(EntityManagerFactory.class);
        EntityManager entityManager = mock(EntityManager.class);
        EntityTransaction transaction = mock(EntityTransaction.class);
        Item item = mock(Item.class);

        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        when(entityManager.getTransaction()).thenReturn(transaction);
        when(item.getName()).thenReturn("Kitchen_Persons");
        when(item.getState()).thenReturn(new DecimalType(1));
        when(transaction.isActive()).thenReturn(false);

        RuntimeException commitFailure = new RuntimeException("commit failed");
        org.mockito.Mockito.doThrow(commitFailure).when(transaction).commit();

        JpaPersistenceService service = new TestJpaPersistenceService(entityManagerFactory);

        assertDoesNotThrow(() -> service.store(item));

        verify(transaction, never()).rollback();
        verify(entityManager).close();
    }

    @Test
    void storeRollsBackActiveTransactionAfterCommitFailure() {
        EntityManagerFactory entityManagerFactory = mock(EntityManagerFactory.class);
        EntityManager entityManager = mock(EntityManager.class);
        EntityTransaction transaction = mock(EntityTransaction.class);
        Item item = mock(Item.class);

        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        when(entityManager.getTransaction()).thenReturn(transaction);
        when(item.getName()).thenReturn("Kitchen_Persons");
        when(item.getState()).thenReturn(new DecimalType(1));
        when(transaction.isActive()).thenReturn(true);

        RuntimeException commitFailure = new RuntimeException("commit failed");
        org.mockito.Mockito.doThrow(commitFailure).when(transaction).commit();

        JpaPersistenceService service = new TestJpaPersistenceService(entityManagerFactory);

        assertDoesNotThrow(() -> service.store(item));

        verify(transaction).rollback();
        verify(entityManager).close();
    }

    private static final class TestJpaPersistenceService extends JpaPersistenceService {
        private final EntityManagerFactory entityManagerFactory;

        TestJpaPersistenceService(EntityManagerFactory entityManagerFactory) {
            super(mock(BundleContext.class), validConfig(), mock(ItemRegistry.class));
            this.entityManagerFactory = entityManagerFactory;
        }

        @Override
        protected EntityManagerFactory getEntityManagerFactory() {
            return entityManagerFactory;
        }
    }

    private static Map<String, @Nullable Object> validConfig() {
        return Map.of("url", "jdbc:derby:memory:test;create=true", "driver", "org.apache.derby.jdbc.EmbeddedDriver");
    }
}
