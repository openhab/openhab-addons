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
package org.openhab.automation.java223.internal.codegeneration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.automation.type.ModuleTypeRegistry;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Tests for the {@link SourceGenerator}, especially the handling of OSGi services which may disappear between
 * the moment they are looked up and the moment they are acquired.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class SourceGeneratorTest {

    private static final String AVAILABLE_SCOPE = "available";
    private static final String DISAPPEARING_SCOPE = "disappearing";

    @Mock
    @NonNullByDefault({})
    private BundleContext bundleContext;

    @Mock
    @NonNullByDefault({})
    private ItemRegistry itemRegistry;

    @Mock
    @NonNullByDefault({})
    private ThingRegistry thingRegistry;

    @Mock
    @NonNullByDefault({})
    private ModuleTypeRegistry moduleTypeRegistry;

    @TempDir
    @NonNullByDefault({})
    private Path tempDir;

    @NonNullByDefault({})
    private SourceGenerator sourceGenerator;

    @BeforeEach
    public void setUp() {
        SourceWriter sourceWriter = new SourceWriter(tempDir);
        DependencyGenerator dependencyGenerator = new DependencyGenerator(tempDir, bundleContext);
        sourceGenerator = new SourceGenerator(sourceWriter, dependencyGenerator, mock(InjectedCodeGenerator.class),
                itemRegistry, thingRegistry, moduleTypeRegistry, bundleContext);
    }

    /**
     * A service can be unregistered at any time: {@link BundleContext#getServiceReferences(Class, String)} returns
     * the references which were registered at the time of the call, but the service behind a given reference may
     * already be gone when {@link BundleContext#getService(ServiceReference)} is called, in which case {@code null}
     * is returned.
     *
     * Such a service must simply be skipped, and its reference must not be released, as it was never acquired. The
     * other services must still be processed.
     */
    @Test
    public void disappearingServiceIsSkippedAndOnlyAcquiredReferencesAreReleased() throws Exception {
        @SuppressWarnings("unchecked")
        ServiceReference<ThingActions> availableReference = mock(ServiceReference.class);
        @SuppressWarnings("unchecked")
        ServiceReference<ThingActions> disappearingReference = mock(ServiceReference.class);

        when(bundleContext.getServiceReferences(ThingActions.class, null))
                .thenReturn(List.of(availableReference, disappearingReference));
        when(bundleContext.getService(availableReference)).thenReturn(new AvailableThingActions());
        // the service behind this reference has been unregistered in the meantime
        doReturn(null).when(bundleContext).getService(disappearingReference);

        // when
        Method method = SourceGenerator.class.getDeclaredMethod("internalGenerateActions");
        method.setAccessible(true);
        method.invoke(sourceGenerator);

        // then the unavailable service is skipped: no class is generated for it
        assertFalse(Files.exists(generatedPath(DISAPPEARING_SCOPE, DisappearingThingActions.class)),
                "The unavailable service must not be generated");
        // ... while the available one is still generated
        assertTrue(Files.exists(generatedPath(AVAILABLE_SCOPE, AvailableThingActions.class)),
                "The available service must be generated");

        // and only the reference which was really acquired is released
        verify(bundleContext).ungetService(availableReference);
        verify(bundleContext, never()).ungetService(disappearingReference);
    }

    private Path generatedPath(String scope, Class<?> actionClass) {
        String packageName = SourceWriter.getPackageName(SourceGenerator.GENERATED, scope);
        return tempDir.resolve(packageName.replace('.', File.separatorChar))
                .resolve(actionClass.getSimpleName() + ".java");
    }

    @ThingActionsScope(name = AVAILABLE_SCOPE)
    public static class AvailableThingActions implements ThingActions {

        @RuleAction(label = "Available action")
        public void availableAction() {
            // nothing to do, this method only exists to be detected as an action
        }

        @Override
        public void setThingHandler(ThingHandler handler) {
            // nothing to do
        }

        @Override
        public @Nullable ThingHandler getThingHandler() {
            return null;
        }
    }

    @ThingActionsScope(name = DISAPPEARING_SCOPE)
    public static class DisappearingThingActions implements ThingActions {

        @RuleAction(label = "Disappearing action")
        public void disappearingAction() {
            // nothing to do, this method only exists to be detected as an action
        }

        @Override
        public void setThingHandler(ThingHandler handler) {
            // nothing to do
        }

        @Override
        public @Nullable ThingHandler getThingHandler() {
            return null;
        }
    }
}
