package org.openhab.binding.jellyfin.internal.api.version.legacy;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class YearsApi {
    private ApiClient apiClient;

    public YearsApi() {
        this(new ApiClient());
    }

    @Autowired
    public YearsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Gets a year.
     * 
     * <p>
     * <b>200</b> - Year returned.
     * <p>
     * <b>404</b> - Year not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param year The year.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getYearRequestCreation(Integer year, UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'year' is set
        if (year == null) {
            throw new WebClientResponseException("Missing the required parameter 'year' when calling getYear",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("year", year);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return apiClient.invokeAPI("/Years/{year}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a year.
     * 
     * <p>
     * <b>200</b> - Year returned.
     * <p>
     * <b>404</b> - Year not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param year The year.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDto> getYear(Integer year, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getYearRequestCreation(year, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a year.
     * 
     * <p>
     * <b>200</b> - Year returned.
     * <p>
     * <b>404</b> - Year not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param year The year.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return ResponseEntity&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDto>> getYearWithHttpInfo(Integer year, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getYearRequestCreation(year, userId).toEntity(localVarReturnType);
    }

    /**
     * Gets a year.
     * 
     * <p>
     * <b>200</b> - Year returned.
     * <p>
     * <b>404</b> - Year not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param year The year.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getYearWithResponseSpec(Integer year, UUID userId) throws WebClientResponseException {
        return getYearRequestCreation(year, userId);
    }

    /**
     * Get years.
     * 
     * <p>
     * <b>200</b> - Year query returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param startIndex Skips over a given number of items within the results. Use for paging.
     * @param limit Optional. The maximum number of records to return.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param excludeItemTypes Optional. If specified, results will be excluded based on item type. This allows
     *            multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be included based on item type. This allows
     *            multiple, comma delimited.
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param userId User Id.
     * @param recursive Search recursively.
     * @param enableImages Optional. Include image information in output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getYearsRequestCreation(Integer startIndex, Integer limit, List<SortOrder> sortOrder,
            UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes,
            List<BaseItemKind> includeItemTypes, List<String> mediaTypes, List<String> sortBy, Boolean enableUserData,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, UUID userId, Boolean recursive,
            Boolean enableImages) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "sortOrder", sortOrder));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "parentId", parentId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "excludeItemTypes", excludeItemTypes));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "includeItemTypes", includeItemTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "mediaTypes", mediaTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "sortBy", sortBy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "enableImageTypes", enableImageTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "recursive", recursive));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/Years", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get years.
     * 
     * <p>
     * <b>200</b> - Year query returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param startIndex Skips over a given number of items within the results. Use for paging.
     * @param limit Optional. The maximum number of records to return.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param excludeItemTypes Optional. If specified, results will be excluded based on item type. This allows
     *            multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be included based on item type. This allows
     *            multiple, comma delimited.
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param userId User Id.
     * @param recursive Search recursively.
     * @param enableImages Optional. Include image information in output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getYears(Integer startIndex, Integer limit, List<SortOrder> sortOrder,
            UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes,
            List<BaseItemKind> includeItemTypes, List<String> mediaTypes, List<String> sortBy, Boolean enableUserData,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, UUID userId, Boolean recursive,
            Boolean enableImages) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getYearsRequestCreation(startIndex, limit, sortOrder, parentId, fields, excludeItemTypes,
                includeItemTypes, mediaTypes, sortBy, enableUserData, imageTypeLimit, enableImageTypes, userId,
                recursive, enableImages).bodyToMono(localVarReturnType);
    }

    /**
     * Get years.
     * 
     * <p>
     * <b>200</b> - Year query returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param startIndex Skips over a given number of items within the results. Use for paging.
     * @param limit Optional. The maximum number of records to return.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param excludeItemTypes Optional. If specified, results will be excluded based on item type. This allows
     *            multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be included based on item type. This allows
     *            multiple, comma delimited.
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param userId User Id.
     * @param recursive Search recursively.
     * @param enableImages Optional. Include image information in output.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getYearsWithHttpInfo(Integer startIndex, Integer limit,
            List<SortOrder> sortOrder, UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes,
            List<BaseItemKind> includeItemTypes, List<String> mediaTypes, List<String> sortBy, Boolean enableUserData,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, UUID userId, Boolean recursive,
            Boolean enableImages) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getYearsRequestCreation(startIndex, limit, sortOrder, parentId, fields, excludeItemTypes,
                includeItemTypes, mediaTypes, sortBy, enableUserData, imageTypeLimit, enableImageTypes, userId,
                recursive, enableImages).toEntity(localVarReturnType);
    }

    /**
     * Get years.
     * 
     * <p>
     * <b>200</b> - Year query returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param startIndex Skips over a given number of items within the results. Use for paging.
     * @param limit Optional. The maximum number of records to return.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param excludeItemTypes Optional. If specified, results will be excluded based on item type. This allows
     *            multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be included based on item type. This allows
     *            multiple, comma delimited.
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param userId User Id.
     * @param recursive Search recursively.
     * @param enableImages Optional. Include image information in output.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getYearsWithResponseSpec(Integer startIndex, Integer limit, List<SortOrder> sortOrder,
            UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes,
            List<BaseItemKind> includeItemTypes, List<String> mediaTypes, List<String> sortBy, Boolean enableUserData,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, UUID userId, Boolean recursive,
            Boolean enableImages) throws WebClientResponseException {
        return getYearsRequestCreation(startIndex, limit, sortOrder, parentId, fields, excludeItemTypes,
                includeItemTypes, mediaTypes, sortBy, enableUserData, imageTypeLimit, enableImageTypes, userId,
                recursive, enableImages);
    }
}
