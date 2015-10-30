/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.karaf.internal;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.Executors;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class acts as a REST resource for addons and provides methods to
 * install and uninstall them.
 * </p>
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
@Path(FeatureResource.PATH_ADDONS)
public class FeatureResource implements RESTResource {

    public static final String PATH_ADDONS = "/addons";

    private final Logger logger = LoggerFactory.getLogger(FeatureResource.class);

    private FeaturesService featureService;

    protected void setFeaturesService(FeaturesService featureService) {
        this.featureService = featureService;
    }

    protected void unsetFeaturesService(FeaturesService featureService) {
        this.featureService = null;
    }

    @Context
    UriInfo uriInfo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAddons() {
        logger.debug("Received HTTP GET request at '{}'", uriInfo.getPath());
        Object responseObject = getAddonBeans(uriInfo.getAbsolutePathBuilder().build());
        return Response.ok(responseObject).build();
    }

    @GET
    @Path("/{addonname: [a-zA-Z_0-9-]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAddon(@Context HttpHeaders headers, @PathParam("addonname") String name) {
        logger.debug("Received HTTP GET request at '{}'.", uriInfo.getPath());
        Object responseObject = getFeatureBean(name);
        if (responseObject != null) {
            return Response.ok(responseObject).build();
        } else {
            return Response.status(404).build();
        }
    }

    @POST
    @Path("/{addonname: [a-zA-Z_0-9-]*}/install")
    public Response installAddon(@PathParam("addonname") final String name) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    featureService.installFeature(Addon.PREFIX + name);
                } catch (Exception e) {
                    logger.error("Exception while installing feature: {}", e.getMessage());
                }
            }
        });
        return Response.ok().build();
    }

    @POST
    @Path("/{addonname: [a-zA-Z_0-9-]*}/uninstall")
    public Response uninstallFeature(@PathParam("addonname") final String name) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    featureService.uninstallFeature(Addon.PREFIX + name);
                } catch (Exception e) {
                    logger.error("Exception while installing feature: {}", e.getMessage());
                }
            }
        });
        return Response.ok().build();
    }

    /* default */ Object getAddonBeans(URI uri) {
        Collection<Addon> beans = new LinkedList<Addon>();
        logger.debug("Received HTTP GET request at '{}'.", UriBuilder.fromUri(uri).build().toASCIIString());
        try {
            for (Feature feature : featureService.listFeatures()) {
                if (feature.getName().startsWith(Addon.PREFIX)
                        && Arrays.asList(FeatureInstaller.addonTypes).contains(getType(feature.getName()))) {
                    Addon bean = getAddonBean(feature);
                    beans.add(bean);
                }
            }
        } catch (Exception e) {
            logger.error("Exception while retrieving features: {}", e.getMessage());
            return Collections.emptyList();
        }
        return beans;
    }

    /* default */ Addon getFeatureBean(String name) {
        Feature feature;
        try {
            feature = featureService.getFeature(Addon.PREFIX + name);
            return getAddonBean(feature);
        } catch (Exception e) {
            logger.error("Exception while querying feature '{}'", name);
            return null;
        }
    }

    private Addon getAddonBean(Feature feature) {
        Addon bean = new Addon();
        bean.id = feature.getId();
        bean.name = getType(feature.getName()) + "-" + getName(feature.getName());
        bean.type = getType(feature.getName());
        bean.description = feature.getDescription();
        bean.version = feature.getVersion();
        bean.isInstalled = featureService.isInstalled(feature);
        return bean;
    }

    private String getType(String name) {
        if (name.startsWith(Addon.PREFIX)) {
            name = name.substring(Addon.PREFIX.length());
            return StringUtils.substringBefore(name, "-");
        }
        return "";
    }

    private String getName(String name) {
        if (name.startsWith(Addon.PREFIX)) {
            name = name.substring(Addon.PREFIX.length());
            return StringUtils.substringAfter(name, "-");
        }
        return name;
    }

}
