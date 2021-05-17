/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.habpanelfilter.internal.web;

import java.util.*;
import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.habpanelfilter.internal.HabPanelFilterConfig;
import org.openhab.core.auth.Role;
import org.openhab.core.io.rest.*;
import org.openhab.core.io.rest.core.item.EnrichedItemDTO;
import org.openhab.core.io.rest.core.item.EnrichedItemDTOMapper;
import org.openhab.core.items.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JSONRequired;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationSelect;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsName;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Pavel Cuchriajev
 */
@Component
@JaxrsResource
@JaxrsName(FilteredItemResource.PATH_FILTERED_ITEMS)
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + RESTConstants.JAX_RS_NAME + ")")
@JSONRequired
@Path(FilteredItemResource.PATH_FILTERED_ITEMS)
@Tag(name = FilteredItemResource.PATH_FILTERED_ITEMS)
@NonNullByDefault
public class FilteredItemResource implements RESTResource {

    public static final String PATH_FILTERED_ITEMS = "items-filtered";

    private static void respectForwarded(final UriBuilder uriBuilder, final @Context HttpHeaders httpHeaders) {
        Optional.ofNullable(httpHeaders.getHeaderString("X-Forwarded-Host")).ifPresent(host -> {
            final String[] parts = host.split(":");
            uriBuilder.host(parts[0]);
            if (parts.length > 1) {
                uriBuilder.port(Integer.parseInt(parts[1]));
            }
        });
        Optional.ofNullable(httpHeaders.getHeaderString("X-Forwarded-Proto")).ifPresent(uriBuilder::scheme);
    }

    private final Logger logger = LoggerFactory.getLogger(FilteredItemResource.class);

    private final DTOMapper dtoMapper;
    private final ItemRegistry itemRegistry;
    private final LocaleService localeService;
    private final ManagedItemProvider managedItemProvider;

    @Activate
    public FilteredItemResource(//
            final @Reference DTOMapper dtoMapper, //
            final @Reference ItemRegistry itemRegistry, //
            final @Reference LocaleService localeService, //
            final @Reference ManagedItemProvider managedItemProvider) {
        logger.info("FilteredItemResource created ...");
        this.dtoMapper = dtoMapper;
        this.itemRegistry = itemRegistry;
        this.localeService = localeService;
        this.managedItemProvider = managedItemProvider;
    }

    private UriBuilder uriBuilder(final UriInfo uriInfo, final HttpHeaders httpHeaders) {
        final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        respectForwarded(uriBuilder, httpHeaders);
        return uriBuilder;
    }

    @GET
    @RolesAllowed({ Role.USER, Role.ADMIN })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getItems", summary = "Get all available items.", responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = EnrichedItemDTO.class)))) })
    public Response getItems(final @Context UriInfo uriInfo, final @Context HttpHeaders httpHeaders,
            @HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @Parameter(description = "language") @Nullable String language,
            @QueryParam("type") @Parameter(description = "item type filter") @Nullable String type,
            @QueryParam("tags") @Parameter(description = "item tag filter") @Nullable String tags,
            @QueryParam("metadata") @Parameter(description = "metadata selector") @Nullable String namespaceSelector,
            @DefaultValue("false") @QueryParam("recursive") @Parameter(description = "get member items recursively") boolean recursive,
            @QueryParam("fields") @Parameter(description = "limit output to the given fields (comma separated)") @Nullable String fields) {
        final Locale locale = localeService.getLocale(language);

        final UriBuilder uriBuilder = uriBuilder(uriInfo, httpHeaders);
        uriBuilder.path("{itemName}");

        Stream<EnrichedItemDTO> itemStream = getItems(type, tags).stream() //
                .map(item -> EnrichedItemDTOMapper.map(item, recursive, null, uriBuilder, locale)) //
                .peek(dto -> dto.editable = isEditable(dto.name));
        itemStream = itemStream.filter(item -> item.groupNames.contains(HabPanelFilterConfig.FILTER_GROUP_NAME));
        itemStream = dtoMapper.limitToFields(itemStream, fields);
        return Response.ok(new Stream2JSONInputStream(itemStream)).build();
    }

    @GET
    @RolesAllowed({ Role.USER, Role.ADMIN })
    @Path("/{itemname: [a-zA-Z_0-9]+}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "getItemByName", summary = "Gets a single item.", responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = EnrichedItemDTO.class))),
            @ApiResponse(responseCode = "404", description = "Item not found") })
    public Response getItemData(final @Context UriInfo uriInfo, final @Context HttpHeaders httpHeaders,
            @HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @Parameter(description = "language") @Nullable String language,
            @QueryParam("metadata") @Parameter(description = "metadata selector") @Nullable String namespaceSelector,
            @PathParam("itemname") @Parameter(description = "item name") String itemname) {
        final Locale locale = localeService.getLocale(language);

        // get item
        Item item = getItem(itemname);

        // if it exists
        if (item != null) {
            EnrichedItemDTO dto = EnrichedItemDTOMapper.map(item, true, null, uriBuilder(uriInfo, httpHeaders), locale);
            dto.editable = isEditable(dto.name);
            return JSONResponse.createResponse(Status.OK, dto, null);
        } else {
            return getItemNotFoundResponse(itemname);
        }
    }

    @GET
    @RolesAllowed({ Role.USER, Role.ADMIN })
    @Path("/{itemname: [a-zA-Z_0-9]+}/state")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(operationId = "getItemState", summary = "Gets the state of an item.", responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Item not found") })
    public Response getPlainItemState(@PathParam("itemname") @Parameter(description = "item name") String itemname) {
        // get item
        Item item = getItem(itemname);

        // if it exists
        if (item != null) {
            // we cannot use JSONResponse.createResponse() bc. MediaType.TEXT_PLAIN
            // return JSONResponse.createResponse(Status.OK, item.getState().toString(), null);
            return Response.ok(item.getState().toFullString()).build();
        } else {
            return getItemNotFoundResponse(itemname);
        }
    }

    private static Response getItemNotFoundResponse(String itemname) {
        String message = "Item " + itemname + " does not exist!";
        return JSONResponse.createResponse(Status.NOT_FOUND, null, message);
    }

    private @Nullable Item getItem(String itemname) {
        return itemRegistry.get(itemname);
    }

    private Collection<Item> getItems(@Nullable String type, @Nullable String tags) {
        Collection<Item> items;
        if (tags == null) {
            if (type == null) {
                items = itemRegistry.getItems();
            } else {
                items = itemRegistry.getItemsOfType(type);
            }
        } else {
            String[] tagList = tags.split(",");
            if (type == null) {
                items = itemRegistry.getItemsByTag(tagList);
            } else {
                items = itemRegistry.getItemsByTagAndType(type, tagList);
            }
        }

        return items;
    }

    private boolean isEditable(String itemName) {
        return managedItemProvider.get(itemName) != null;
    }
}
