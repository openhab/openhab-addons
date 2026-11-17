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
package org.openhab.binding.knx.internal.rest;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.core.auth.Role;

/**
 * Checks by reflection that the REST resource is for admins only: the class and every endpoint method carry the
 * admin role, and every public method is an endpoint, so a method added later without the role fails here. The
 * whiteboard properties have class retention and are checked in the built jar instead.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxResourceRolesTest {

    private static List<Method> publicMethods() {
        return Arrays.stream(KnxResource.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()) && !method.isSynthetic()).toList();
    }

    private static boolean isEndpoint(Method method) {
        return method.isAnnotationPresent(GET.class) || method.isAnnotationPresent(PUT.class)
                || method.isAnnotationPresent(POST.class) || method.isAnnotationPresent(DELETE.class);
    }

    private static void assertAdminOnly(@Nullable RolesAllowed roles, String where) {
        assertNotNull(roles, where + " has no @RolesAllowed");
        assertArrayEquals(new String[] { Role.ADMIN }, roles.value(), where + " is not for admins only");
    }

    @Test
    void theClassIsForAdminsOnlyUnderKnx() {
        assertAdminOnly(KnxResource.class.getAnnotation(RolesAllowed.class), "the class");
        Path path = KnxResource.class.getAnnotation(Path.class);
        assertNotNull(path);
        assertEquals("knx", path.value());
    }

    @Test
    void everyPublicMethodIsAnEndpointForAdminsOnly() {
        List<Method> methods = publicMethods();
        assertFalse(methods.isEmpty());
        for (Method method : methods) {
            assertTrue(isEndpoint(method), method.getName() + " is public but no endpoint");
            assertAdminOnly(method.getAnnotation(RolesAllowed.class), method.getName());
        }
    }

    @Test
    void noEndpointParameterIsAPrimitiveOrAList() {
        for (Method method : publicMethods()) {
            for (Parameter parameter : method.getParameters()) {
                Class<?> type = parameter.getType();
                String where = method.getName() + " " + parameter.getName();
                assertFalse(type.isPrimitive(), where + " is a primitive");
                assertFalse(type.isArray(), where + " is an array");
                assertFalse(Collection.class.isAssignableFrom(type), where + " is a list");
                assertFalse(Map.class.isAssignableFrom(type), where + " is a map");
            }
        }
    }
}
