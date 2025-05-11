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
public class StudiosApi {
    private ApiClient apiClient;

    public StudiosApi() {
        this(new ApiClient());
    }

    @Autowired
    public StudiosApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Gets a studio by name.
     * 
     * <p>
     * <b>200</b> - Studio returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name Studio name.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getStudioRequestCreation(String name, UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getStudio",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);

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
        return apiClient.invokeAPI("/Studios/{name}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a studio by name.
     * 
     * <p>
     * <b>200</b> - Studio returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name Studio name.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDto> getStudio(String name, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getStudioRequestCreation(name, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a studio by name.
     * 
     * <p>
     * <b>200</b> - Studio returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name Studio name.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return ResponseEntity&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDto>> getStudioWithHttpInfo(String name, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getStudioRequestCreation(name, userId).toEntity(localVarReturnType);
    }

    /**
     * Gets a studio by name.
     * 
     * <p>
     * <b>200</b> - Studio returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name Studio name.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getStudioWithResponseSpec(String name, UUID userId) throws WebClientResponseException {
        return getStudioRequestCreation(name, userId);
    }

    /**
     * Gets all studios from a given item, folder, or the entire library.
     * 
     * <p>
     * <b>200</b> - Studios returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param searchTerm Optional. Search term.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows
     *            multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param userId User id.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param enableImages Optional, include image information in output.
     * @param enableTotalRecordCount Total record count.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getStudiosRequestCreation(Integer startIndex, Integer limit, String searchTerm, UUID parentId,
            List<ItemFields> fields, List<BaseItemKind> excludeItemTypes, List<BaseItemKind> includeItemTypes,
            Boolean isFavorite, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes,
            UUID userId, String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan,
            Boolean enableImages, Boolean enableTotalRecordCount) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "searchTerm", searchTerm));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "parentId", parentId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "excludeItemTypes", excludeItemTypes));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "includeItemTypes", includeItemTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isFavorite", isFavorite));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "enableImageTypes", enableImageTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams
                .putAll(apiClient.parameterToMultiValueMap(null, "nameStartsWithOrGreater", nameStartsWithOrGreater));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "nameStartsWith", nameStartsWith));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "nameLessThan", nameLessThan));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableTotalRecordCount", enableTotalRecordCount));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/Studios", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets all studios from a given item, folder, or the entire library.
     * 
     * <p>
     * <b>200</b> - Studios returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param searchTerm Optional. Search term.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows
     *            multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param userId User id.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param enableImages Optional, include image information in output.
     * @param enableTotalRecordCount Total record count.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getStudios(Integer startIndex, Integer limit, String searchTerm, UUID parentId,
            List<ItemFields> fields, List<BaseItemKind> excludeItemTypes, List<BaseItemKind> includeItemTypes,
            Boolean isFavorite, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes,
            UUID userId, String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan,
            Boolean enableImages, Boolean enableTotalRecordCount) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getStudiosRequestCreation(startIndex, limit, searchTerm, parentId, fields, excludeItemTypes,
                includeItemTypes, isFavorite, enableUserData, imageTypeLimit, enableImageTypes, userId,
                nameStartsWithOrGreater, nameStartsWith, nameLessThan, enableImages, enableTotalRecordCount)
                .bodyToMono(localVarReturnType);
    }

    /**
     * Gets all studios from a given item, folder, or the entire library.
     * 
     * <p>
     * <b>200</b> - Studios returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param searchTerm Optional. Search term.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows
     *            multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param userId User id.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param enableImages Optional, include image information in output.
     * @param enableTotalRecordCount Total record count.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getStudiosWithHttpInfo(Integer startIndex, Integer limit,
            String searchTerm, UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes,
            List<BaseItemKind> includeItemTypes, Boolean isFavorite, Boolean enableUserData, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, UUID userId, String nameStartsWithOrGreater, String nameStartsWith,
            String nameLessThan, Boolean enableImages, Boolean enableTotalRecordCount)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getStudiosRequestCreation(startIndex, limit, searchTerm, parentId, fields, excludeItemTypes,
                includeItemTypes, isFavorite, enableUserData, imageTypeLimit, enableImageTypes, userId,
                nameStartsWithOrGreater, nameStartsWith, nameLessThan, enableImages, enableTotalRecordCount)
                .toEntity(localVarReturnType);
    }

    /**
     * Gets all studios from a given item, folder, or the entire library.
     * 
     * <p>
     * <b>200</b> - Studios returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param searchTerm Optional. Search term.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows
     *            multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param userId User id.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param enableImages Optional, include image information in output.
     * @param enableTotalRecordCount Total record count.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getStudiosWithResponseSpec(Integer startIndex, Integer limit, String searchTerm, UUID parentId,
            List<ItemFields> fields, List<BaseItemKind> excludeItemTypes, List<BaseItemKind> includeItemTypes,
            Boolean isFavorite, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes,
            UUID userId, String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan,
            Boolean enableImages, Boolean enableTotalRecordCount) throws WebClientResponseException {
        return getStudiosRequestCreation(startIndex, limit, searchTerm, parentId, fields, excludeItemTypes,
                includeItemTypes, isFavorite, enableUserData, imageTypeLimit, enableImageTypes, userId,
                nameStartsWithOrGreater, nameStartsWith, nameLessThan, enableImages, enableTotalRecordCount);
    }
}
