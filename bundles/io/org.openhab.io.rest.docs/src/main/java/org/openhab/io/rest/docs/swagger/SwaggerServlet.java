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

import static org.openhab.io.rest.docs.swagger.Constants.HEADER_X_FORWARDED_PROTO;
import static org.openhab.io.rest.docs.swagger.SwaggerUtil.documentOperations;
import static org.openhab.io.rest.docs.swagger.SwaggerUtil.getDescription;
import static org.openhab.io.rest.docs.swagger.SwaggerUtil.getPath;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.openhab.io.rest.docs.Description;
import org.openhab.io.rest.docs.NoDocumentation;
import org.openhab.io.rest.docs.swagger.model.SwaggerAPI;
import org.openhab.io.rest.docs.swagger.model.SwaggerAPIPath;
import org.openhab.io.rest.docs.swagger.model.SwaggerModel;
import org.openhab.io.rest.docs.swagger.model.SwaggerOperation;
import org.openhab.io.rest.docs.swagger.model.SwaggerResource;
import org.openhab.io.rest.docs.swagger.model.SwaggerResources;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Listens to services in the framework and analyzes them for documentation. Also implements
 * the Servlet API so you can map this component to an endpoint and use it to show all
 * REST APIs that are available in the framework.
 */
@SuppressWarnings("serial")
public class SwaggerServlet extends HttpServlet implements ManagedService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
    private static final String ENDPOINT_KEY = "endpoint";
    private static final String DEFAULT_ENDPOINT = "/rest";

    private static final JaxRsMethodComparator COMPARATOR = new JaxRsMethodComparator();

    private final CopyOnWriteArrayList<Object> m_restServices = new CopyOnWriteArrayList<Object>();

    private String m_restEndpoint = DEFAULT_ENDPOINT;

    private HttpService httpService;

    public void addService(Object service) {
        Class<?> clazz = service.getClass();

        // Check whether it is a "real" JAX-RS component, and whether we should generate documentation for it...
        if (clazz.isAnnotationPresent(Path.class) && !clazz.isAnnotationPresent(NoDocumentation.class)) {
            m_restServices.addIfAbsent(service);
        }
    }

    public void removeService(Object service) {
        m_restServices.remove(service);
    }

    protected void setHttpService(HttpService httpService) {
    	this.httpService = httpService;
    }
    
    protected void unsetHttpService(HttpService httpService) {
    	this.httpService = null;
    }
    
    protected void activate() {
    	try {
			httpService.registerServlet("/restdocs", this, new Hashtable<>(), httpService.createDefaultHttpContext());
			httpService.registerResources("/doc", "swagger", httpService.createDefaultHttpContext());
		} catch (ServletException e) {
			logger.error("Could not start up REST documentation service: {}", e.getMessage());
		} catch (NamespaceException e) {
			logger.error("Could not start up REST documentation service: {}", e.getMessage());
		}
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public void updated(Dictionary properties) throws ConfigurationException {
        if (properties == null) {
            m_restEndpoint = DEFAULT_ENDPOINT;
        } else {
            Object endpoint = properties.get(ENDPOINT_KEY);
            if (endpoint instanceof String) {
                m_restEndpoint = (String) endpoint;
            } else {
                throw new ConfigurationException(ENDPOINT_KEY, "has to be a non-null string.");
            }
        }
    }

    protected SwaggerAPI createDocumentationFor(String baseURL, Class<?> clazz) {
        String rootPath = getPath(clazz.getAnnotation(Path.class));

        // See section 3.3.1 of the JAX-RS specification v1.1...
        Method[] methods = clazz.getMethods();
        Arrays.sort(methods, COMPARATOR);

        SwaggerModel models = new SwaggerModel();
        List<SwaggerAPIPath> apis = new ArrayList<SwaggerAPIPath>();

        for (Method method : methods) {
            if (method.isAnnotationPresent(NoDocumentation.class)) {
                // Do not create documentation for this method...
                continue;
            }

            List<SwaggerOperation> ops = documentOperations(models, method);
            if (ops.isEmpty()) {
                // Not a valid JAX-RS operation...
                continue;
            }

            String path = getPath(method.getAnnotation(Path.class));
            String doc = getDescription(method.getAnnotation(Description.class));

            apis.add(new SwaggerAPIPath(rootPath.concat(path), doc, ops));
        }

        return new SwaggerAPI(baseURL.concat(m_restEndpoint), rootPath, apis, models);
    }

    protected SwaggerResources createResourceListingFor(String baseURL, String rootPath, Map<String, Class<?>> services) {
        List<SwaggerResource> rs = new ArrayList<SwaggerResource>();
        for (Entry<String, Class<?>> entry : services.entrySet()) {
            Class<?> serviceType = entry.getValue();
            String path = getPath(entry.getKey());
            String doc = getDescription(serviceType.getAnnotation(Description.class));

            rs.add(new SwaggerResource(rootPath.concat(path), doc));
        }
        return new SwaggerResources(baseURL, rs);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String baseURL = getBaseURL(req);
        String requestPath = getPath(req.getPathInfo());

        SortedMap<String, Class<?>> services = getServices();
        if ("".equals(requestPath) || "/".equals(requestPath)) {
            SwaggerResources resources = createResourceListingFor(baseURL, req.getServletPath(), services);

            writeAsJSON(resp, resources);
        } else {
            Class<?> serviceType = services.get(requestPath);
            if (serviceType != null) {
                SwaggerAPI apiDocs = createDocumentationFor(baseURL, serviceType);

                writeAsJSON(resp, apiDocs);
            } else {
                // Swagger-UI b0rks when returning anything other than a valid JSON response,
                // so in case we didn't write anything, simply return an empty JSON string...
                writeAsJSON(resp, "");
            }
        }
    }

    private String getBaseURL(HttpServletRequest request) {
        int port = request.getServerPort();

        String protocol = request.getHeader(HEADER_X_FORWARDED_PROTO);
        if (protocol != null) {
            if ("https".equals(protocol)) {
                port = 443;
            }
        } else {
            protocol = request.isSecure() ? "https" : "http";
        }

        return protocol + "://" + request.getServerName() + ":" + port;
    }

    private SortedMap<String, Class<?>> getServices() {
        SortedMap<String, Class<?>> result = new TreeMap<>();
        for (Object service : m_restServices) {
            Class<?> serviceType = service.getClass();
            String path = getPath(serviceType.getAnnotation(Path.class));

            result.put(path, serviceType);
        }
        return result;
    }

    private void writeAsJSON(HttpServletResponse resp, Object object) throws IOException {
        Gson gson = new Gson();
        resp.setContentType(MediaType.APPLICATION_JSON);
        PrintWriter writer = resp.getWriter();
        try {
            writer.append(gson.toJson(object));
        } finally {
            writer.flush();
            writer.close();
        }
    }

    private static class JaxRsMethodComparator implements Comparator<Method> {
        @Override
        public int compare(Method o1, Method o2) {
            Path pa1 = o1.getAnnotation(Path.class);
            String p1 = (pa1 != null) ? pa1.value() : "";

            Path pa2 = o2.getAnnotation(Path.class);
            String p2 = (pa2 != null) ? pa2.value() : "";

            int result = p1.compareTo(p2);
            if (result == 0) {
                result = o1.getName().compareTo(o2.getName());
            }

            return result;
        }
    }
}
