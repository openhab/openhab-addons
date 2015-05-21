/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.io.rest.docs.swagger;

import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.HEAD;
import static javax.ws.rs.HttpMethod.OPTIONS;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.HttpMethod.PUT;
import static org.openhab.io.rest.docs.swagger.Constants.BODY_PARAM;
import static org.openhab.io.rest.docs.swagger.Constants.FORM_PARAM;
import static org.openhab.io.rest.docs.swagger.Constants.HEADER_PARAM;
import static org.openhab.io.rest.docs.swagger.Constants.PATH_PARAM;
import static org.openhab.io.rest.docs.swagger.Constants.QUERY_PARAM;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.openhab.io.rest.docs.Description;
import org.openhab.io.rest.docs.Notes;
import org.openhab.io.rest.docs.ResponseMessage;
import org.openhab.io.rest.docs.ResponseMessages;
import org.openhab.io.rest.docs.ReturnType;
import org.openhab.io.rest.docs.swagger.model.SwaggerContainerType;
import org.openhab.io.rest.docs.swagger.model.SwaggerDataType;
import org.openhab.io.rest.docs.swagger.model.SwaggerModel;
import org.openhab.io.rest.docs.swagger.model.SwaggerModelProperty;
import org.openhab.io.rest.docs.swagger.model.SwaggerModelType;
import org.openhab.io.rest.docs.swagger.model.SwaggerOperation;
import org.openhab.io.rest.docs.swagger.model.SwaggerParameter;
import org.openhab.io.rest.docs.swagger.model.SwaggerResponseMessage;

final class SwaggerUtil {

    public static List<SwaggerOperation> documentOperations(SwaggerModel models, Method method) {
        List<SwaggerOperation> ops = new ArrayList<SwaggerOperation>();

        Annotation[] annotations = method.getAnnotations();
        if (annotationPresent(GET.class, annotations)) {
            ops.add(createOperation(models, GET, method, annotations));
        }
        if (annotationPresent(PUT.class, annotations)) {
            ops.add(createOperation(models, PUT, method, annotations));
        }
        if (annotationPresent(POST.class, annotations)) {
            ops.add(createOperation(models, POST, method, annotations));
        }
        if (annotationPresent(DELETE.class, annotations)) {
            ops.add(createOperation(models, DELETE, method, annotations));
        }
        if (annotationPresent(HEAD.class, annotations)) {
            ops.add(createOperation(models, HEAD, method, annotations));
        }
        if (annotationPresent(OPTIONS.class, annotations)) {
            ops.add(createOperation(models, OPTIONS, method, annotations));
        }

        return ops;
    }

    public static List<SwaggerParameter> documentParameters(SwaggerModel models, Method method) {
        List<SwaggerParameter> ps = new ArrayList<SwaggerParameter>();

        Annotation[][] parametersAnnotations = method.getParameterAnnotations();
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramType = parameterTypes[i];
            Annotation[] paramAnnotations = parametersAnnotations[i];

            boolean consumed = false;
            for (Annotation a : paramAnnotations) {
                if (a instanceof PathParam) {
                    ps.add(createParameter(models, PATH_PARAM, ((PathParam) a).value(), paramType, paramAnnotations));
                    consumed = true;
                } else if (a instanceof QueryParam) {
                    ps.add(createParameter(models, QUERY_PARAM, ((QueryParam) a).value(), paramType, paramAnnotations));
                    consumed = true;
                } else if (a instanceof HeaderParam) {
                    ps.add(createParameter(models, HEADER_PARAM, ((HeaderParam) a).value(), paramType, paramAnnotations));
                    consumed = true;
                } else if (a instanceof FormParam) {
                    ps.add(createParameter(models, FORM_PARAM, ((FormParam) a).value(), paramType, paramAnnotations));
                    consumed = true;
                }
                if (a instanceof Context) {
                    // No need to report these ones, they are automatically injected by JAX-RS...
                    consumed = true;
                }
            }

            if (!consumed) {
                // Add all non-consumed parameters as body-parameters (most probably complex types that need to be supplied)...
                ps.add(createParameter(models, BODY_PARAM, paramType.getSimpleName(), paramType, paramAnnotations));
            }
        }

        return ps;
    }

    public static String getDescription(Description ann) {
        return (ann != null) ? ann.value() : null;
    }

    public static String getPath(Path ann) {
        if (ann == null) {
            return "";
        }
        return getPath(ann.value());
    }

    public static String getPath(String path) {
        if (path == null) {
            return "";
        }
        String value = path.trim();
        if (!value.startsWith("/") && value.length() > 1) {
            return "/".concat(value);
        }
        return value;
    }

    protected static SwaggerModelType convertToSwaggerModel(SwaggerModel models, Class<?> type) {
        Map<String, SwaggerModelProperty> mp = new HashMap<String, SwaggerModelProperty>();
        List<String> requiredFields = new ArrayList<String>();
        // AMDATUWEB-24 - use not only field of given type, but also of all its superclasses...
        String typeName = type.getName();
        while (type != null && !Object.class.equals(type)) {
            Field[] fields = type.getDeclaredFields();
            for (Field f : fields) {
                String name = f.getName();
                int modifiers = f.getModifiers();
                Class<?> fieldType = f.getType();
                
                if (mp.containsKey(name)) {
                    // TODO name shadowing is not supported yet...
                    continue;
                } else if (Modifier.isStatic(modifiers) || fieldType.isSynthetic()) {
                    continue;
                }

                Description description = f.getAnnotation(Description.class);
                DefaultValue defaultValue = f.getAnnotation(DefaultValue.class);

                SwaggerDataType typeInfo = convertToSwaggerType(models, fieldType);

                SwaggerModelProperty smp;
                if (fieldType.isEnum()) {
                    smp = new SwaggerModelProperty(typeInfo, getDescription(description), getValue(defaultValue), getEnumValues(fieldType));
                } else if (fieldType.isArray() || Collection.class.isAssignableFrom(fieldType)) {
                    Boolean uniqueItems = Set.class.isAssignableFrom(fieldType) ? Boolean.TRUE : null;
                    smp = new SwaggerModelProperty(typeInfo, getDescription(description), getValue(defaultValue), uniqueItems, getContainerType(models, f));
                } else {
                    smp = new SwaggerModelProperty(typeInfo, getDescription(description), getValue(defaultValue));
                }
                
                if (Boolean.TRUE.equals(smp.required)) {
                    requiredFields.add(name);
                }
                
                mp.put(name, smp);
            }
            type = type.getSuperclass();
        }
        return new SwaggerModelType(typeName, mp, requiredFields);
    }

    protected static Class<?> getReturnType(Method method) {
    	ReturnType annotation = method.getAnnotation(ReturnType.class);
    	if (annotation != null) {
    		return annotation.value();
    	}
    	
    	return method.getReturnType();
    }
    
    /**
     * @see https://github.com/wordnik/swagger-core/wiki/datatypes
     */
    protected static SwaggerDataType convertToSwaggerType(SwaggerModel models, Class<?> type) {
        if (Void.TYPE.equals(type)) {
            return new SwaggerDataType("void");
        } else if (Integer.TYPE.equals(type) || Integer.class.isAssignableFrom(type)) {
            return new SwaggerDataType("integer", "int32");
        } else if (Long.TYPE.equals(type) || Long.class.isAssignableFrom(type)) {
            return new SwaggerDataType("integer", "int64");
        } else if (Float.TYPE.equals(type) || Float.class.isAssignableFrom(type)) {
            return new SwaggerDataType("number", "float");
        } else if (Double.TYPE.equals(type) || Double.class.isAssignableFrom(type)) {
            return new SwaggerDataType("number", "double");
        } else if (Byte.TYPE.equals(type) || Byte.class.isAssignableFrom(type)) {
            return new SwaggerDataType("string", "byte");
        } else if (Boolean.TYPE.equals(type) || Boolean.class.isAssignableFrom(type)) {
            return new SwaggerDataType("boolean");
        } else if (Number.class.isAssignableFrom(type)) {
            return new SwaggerDataType("number");
        } else if (String.class.equals(type)) {
            return new SwaggerDataType("string");
        } else if (Date.class.equals(type)) {
            return new SwaggerDataType("string", "date-time");
        } else if (type.isEnum()) {
            return new SwaggerDataType("string");
        } else if (type.isArray() || Collection.class.isAssignableFrom(type)) {
            return new SwaggerDataType("array");
        } else {
            // it's a custom type, we need to create a model for it (if it does not already exist)
            String typeName = type.getName();
            if (!models.containsKey(typeName)) {
                // Reserve a spot for this type, avoids circular references to cause a StackOverflow, see AMDATUWEB-10...
                models.put(typeName, null);
                // Overwrite the item with the actual model definition...
                models.put(typeName, convertToSwaggerModel(models, type));
            }
            return new SwaggerDataType(type.getName());
        }
    }

    protected static SwaggerParameter createParameter(SwaggerModel models, String paramType, String paramName,
        Class<?> type, Annotation[] annotations) {
        String doc = getDescription(findAnnotation(Description.class, annotations));
        String _default = getValue(findAnnotation(DefaultValue.class, annotations));

        SwaggerDataType typeInfo = convertToSwaggerType(models, type);

        if (type.isEnum()) {
            return new SwaggerParameter(paramType, paramName, doc, typeInfo, _default, getEnumValues(type));
        }

        return new SwaggerParameter(paramType, paramName, doc, typeInfo, _default);
    }

    private static <T extends Annotation> boolean annotationPresent(Class<T> type, Annotation[] annotations) {
        for (Annotation ann : annotations) {
            if (type.isInstance(ann)) {
                return true;
            }
        }
        return false;
    }

    private static SwaggerOperation createOperation(SwaggerModel models, String httpMethod, Method method, Annotation[] annotations) {
        String opName = method.getName();
        SwaggerDataType returnTypeInfo = convertToSwaggerType(models, getReturnType(method));

        List<SwaggerParameter> params = documentParameters(models, method);

        ResponseMessages responseMessages = findAnnotation(ResponseMessages.class, annotations);
        List<SwaggerResponseMessage> rms = null;
        if (responseMessages != null && responseMessages.value() != null) {
            rms = new ArrayList<SwaggerResponseMessage>();
            for (ResponseMessage responseMessage : responseMessages.value()) {
                rms.add(new SwaggerResponseMessage(responseMessage.code(), responseMessage.message()));
            }
        }

        String doc = getDescription(findAnnotation(Description.class, annotations));
        List<String> produces = getValues(findAnnotation(Produces.class, annotations));
        List<String> consumes = getValues(findAnnotation(Consumes.class, annotations));
        String notes = getValue(findAnnotation(Notes.class, annotations));
        Boolean deprecated = findAnnotation(Deprecated.class, annotations) != null;

        return new SwaggerOperation(httpMethod, opName, returnTypeInfo, params, rms, produces, consumes, doc, notes, deprecated);
    }

    private static <T extends Annotation> T findAnnotation(Class<T> type, Annotation[] annotations) {
        for (Annotation ann : annotations) {
            if (type.isInstance(ann)) {
                return type.cast(ann);
            }
        }
        return null;
    }

    private static SwaggerContainerType getContainerType(SwaggerModel models, Field field) {
        Class<?> type = getContainerType(field.getGenericType());

        String refType = Object.class.getName();
        if (type != null) {
            refType = convertToSwaggerType(models, type).dataType;
        }
        return new SwaggerContainerType(refType);
    }

    /**
     * Magic to determine the actual type of a container.
     * 
     * @param type the supposed container type, can be <code>null</code>.
     * @return a container type, or <code>null</code> if the type could not be determined.
     */
    private static Class<?> getContainerType(Type type) {
        if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            if (clazz.isArray()) {
                return clazz.getComponentType();
            } else {
                return clazz;
            }
        } else if (type instanceof GenericArrayType) {
            Type compType = ((GenericArrayType) type).getGenericComponentType();
            return getContainerType(compType);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;

            Type[] actualTypeArguments = paramType.getActualTypeArguments();
            for (Type t : actualTypeArguments) {
                Class<?> ct = getContainerType(t);
                if (ct != null) {
                    return ct;
                }
            }
        } else if (type instanceof WildcardType) {
            // Upper bound is stronger than lower, so return the this one, if defined...
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            for (Type upperBound : upperBounds) {
                Class<?> ubt = getContainerType(upperBound);
                if (ubt != null) {
                    return ubt;
                }
            }
            Type[] lowerBounds = ((WildcardType) type).getLowerBounds();
            for (Type lowerBound : lowerBounds) {
                Class<?> lbt = getContainerType(lowerBound);
                if (lbt != null) {
                    return lbt;
                }
            }
        }

        return null;
    }

    private static List<String> getEnumValues(Class<?> type) {
        List<String> result = new ArrayList<String>();
        for (Object constant : type.getEnumConstants()) {
            result.add(constant.toString());
        }
        return result;
    }

    private static String getValue(DefaultValue ann) {
        return (ann != null) ? ann.value() : null;
    }

    private static String getValue(Notes ann) {
        return (ann != null) ? ann.value() : null;
    }

    private static List<String> getValues(Consumes ann) {
        return (ann != null) ? Arrays.asList(ann.value()) : null;
    }

    private static List<String> getValues(Produces ann) {
        return (ann != null) ? Arrays.asList(ann.value()) : null;
    }
}