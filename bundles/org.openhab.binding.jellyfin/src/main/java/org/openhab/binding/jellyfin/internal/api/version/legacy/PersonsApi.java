package org.openhab.binding.jellyfin.internal.api.version.legacy;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ItemFilter;
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
public class PersonsApi {
    private ApiClient apiClient;

    public PersonsApi() {
        this(new ApiClient());
    }

    @Autowired
    public PersonsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Get person by name.
     * 
     * <p>
     * <b>200</b> - Person returned.
     * <p>
     * <b>404</b> - Person not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name Person name.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPersonRequestCreation(String name, UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getPerson",
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
        return apiClient.invokeAPI("/Persons/{name}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get person by name.
     * 
     * <p>
     * <b>200</b> - Person returned.
     * <p>
     * <b>404</b> - Person not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name Person name.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDto> getPerson(String name, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getPersonRequestCreation(name, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Get person by name.
     * 
     * <p>
     * <b>200</b> - Person returned.
     * <p>
     * <b>404</b> - Person not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name Person name.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return ResponseEntity&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDto>> getPersonWithHttpInfo(String name, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getPersonRequestCreation(name, userId).toEntity(localVarReturnType);
    }

    /**
     * Get person by name.
     * 
     * <p>
     * <b>200</b> - Person returned.
     * <p>
     * <b>404</b> - Person not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name Person name.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPersonWithResponseSpec(String name, UUID userId) throws WebClientResponseException {
        return getPersonRequestCreation(name, userId);
    }

    /**
     * Gets all persons.
     * 
     * <p>
     * <b>200</b> - Persons returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param limit Optional. The maximum number of records to return.
     * @param searchTerm The search term.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param filters Optional. Specify additional filters to apply.
     * @param isFavorite Optional filter by items that are marked as favorite, or not. userId is required.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param excludePersonTypes Optional. If specified results will be filtered to exclude those containing the
     *            specified PersonType. Allows multiple, comma-delimited.
     * @param personTypes Optional. If specified results will be filtered to include only those containing the specified
     *            PersonType. Allows multiple, comma-delimited.
     * @param appearsInItemId Optional. If specified, person results will be filtered on items related to said persons.
     * @param userId User id.
     * @param enableImages Optional, include image information in output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPersonsRequestCreation(Integer limit, String searchTerm, List<ItemFields> fields,
            List<ItemFilter> filters, Boolean isFavorite, Boolean enableUserData, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, List<String> excludePersonTypes, List<String> personTypes,
            UUID appearsInItemId, UUID userId, Boolean enableImages) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "searchTerm", searchTerm));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "filters", filters));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isFavorite", isFavorite));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "enableImageTypes", enableImageTypes));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "excludePersonTypes", excludePersonTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "personTypes", personTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "appearsInItemId", appearsInItemId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/Persons", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets all persons.
     * 
     * <p>
     * <b>200</b> - Persons returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param limit Optional. The maximum number of records to return.
     * @param searchTerm The search term.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param filters Optional. Specify additional filters to apply.
     * @param isFavorite Optional filter by items that are marked as favorite, or not. userId is required.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param excludePersonTypes Optional. If specified results will be filtered to exclude those containing the
     *            specified PersonType. Allows multiple, comma-delimited.
     * @param personTypes Optional. If specified results will be filtered to include only those containing the specified
     *            PersonType. Allows multiple, comma-delimited.
     * @param appearsInItemId Optional. If specified, person results will be filtered on items related to said persons.
     * @param userId User id.
     * @param enableImages Optional, include image information in output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getPersons(Integer limit, String searchTerm, List<ItemFields> fields,
            List<ItemFilter> filters, Boolean isFavorite, Boolean enableUserData, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, List<String> excludePersonTypes, List<String> personTypes,
            UUID appearsInItemId, UUID userId, Boolean enableImages) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getPersonsRequestCreation(limit, searchTerm, fields, filters, isFavorite, enableUserData, imageTypeLimit,
                enableImageTypes, excludePersonTypes, personTypes, appearsInItemId, userId, enableImages)
                .bodyToMono(localVarReturnType);
    }

    /**
     * Gets all persons.
     * 
     * <p>
     * <b>200</b> - Persons returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param limit Optional. The maximum number of records to return.
     * @param searchTerm The search term.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param filters Optional. Specify additional filters to apply.
     * @param isFavorite Optional filter by items that are marked as favorite, or not. userId is required.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param excludePersonTypes Optional. If specified results will be filtered to exclude those containing the
     *            specified PersonType. Allows multiple, comma-delimited.
     * @param personTypes Optional. If specified results will be filtered to include only those containing the specified
     *            PersonType. Allows multiple, comma-delimited.
     * @param appearsInItemId Optional. If specified, person results will be filtered on items related to said persons.
     * @param userId User id.
     * @param enableImages Optional, include image information in output.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getPersonsWithHttpInfo(Integer limit, String searchTerm,
            List<ItemFields> fields, List<ItemFilter> filters, Boolean isFavorite, Boolean enableUserData,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, List<String> excludePersonTypes,
            List<String> personTypes, UUID appearsInItemId, UUID userId, Boolean enableImages)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getPersonsRequestCreation(limit, searchTerm, fields, filters, isFavorite, enableUserData, imageTypeLimit,
                enableImageTypes, excludePersonTypes, personTypes, appearsInItemId, userId, enableImages)
                .toEntity(localVarReturnType);
    }

    /**
     * Gets all persons.
     * 
     * <p>
     * <b>200</b> - Persons returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param limit Optional. The maximum number of records to return.
     * @param searchTerm The search term.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param filters Optional. Specify additional filters to apply.
     * @param isFavorite Optional filter by items that are marked as favorite, or not. userId is required.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param excludePersonTypes Optional. If specified results will be filtered to exclude those containing the
     *            specified PersonType. Allows multiple, comma-delimited.
     * @param personTypes Optional. If specified results will be filtered to include only those containing the specified
     *            PersonType. Allows multiple, comma-delimited.
     * @param appearsInItemId Optional. If specified, person results will be filtered on items related to said persons.
     * @param userId User id.
     * @param enableImages Optional, include image information in output.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPersonsWithResponseSpec(Integer limit, String searchTerm, List<ItemFields> fields,
            List<ItemFilter> filters, Boolean isFavorite, Boolean enableUserData, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, List<String> excludePersonTypes, List<String> personTypes,
            UUID appearsInItemId, UUID userId, Boolean enableImages) throws WebClientResponseException {
        return getPersonsRequestCreation(limit, searchTerm, fields, filters, isFavorite, enableUserData, imageTypeLimit,
                enableImageTypes, excludePersonTypes, personTypes, appearsInItemId, userId, enableImages);
    }
}
