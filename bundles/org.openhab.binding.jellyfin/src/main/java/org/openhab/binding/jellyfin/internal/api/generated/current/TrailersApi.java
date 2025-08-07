package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemFilter;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemSortBy;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.LocationType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.MediaType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SeriesStatus;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SortOrder;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.VideoType;

import jakarta.ws.rs.core.GenericType;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class TrailersApi {
    private ApiClient apiClient;

    public TrailersApi() {
        this(Configuration.getDefaultApiClient());
    }

    public TrailersApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Get the API client
     *
     * @return API client
     */
    public ApiClient getApiClient() {
        return apiClient;
    }

    /**
     * Set the API client
     *
     * @param apiClient an instance of API client
     */
    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Finds movies and trailers similar to a given trailer.
     * 
     * @param userId The user id supplied as query parameter; this is required when not using an API key. (optional)
     * @param maxOfficialRating Optional filter by maximum official rating (PG, PG-13, TV-MA, etc). (optional)
     * @param hasThemeSong Optional filter by items with theme songs. (optional)
     * @param hasThemeVideo Optional filter by items with theme videos. (optional)
     * @param hasSubtitles Optional filter by items with subtitles. (optional)
     * @param hasSpecialFeature Optional filter by items with special features. (optional)
     * @param hasTrailer Optional filter by items with trailers. (optional)
     * @param adjacentTo Optional. Return items that are siblings of a supplied item. (optional)
     * @param parentIndexNumber Optional filter by parent index number. (optional)
     * @param hasParentalRating Optional filter by items that have or do not have a parental rating. (optional)
     * @param isHd Optional filter by items that are HD or not. (optional)
     * @param is4K Optional filter by items that are 4K or not. (optional)
     * @param locationTypes Optional. If specified, results will be filtered based on LocationType. This allows
     *            multiple, comma delimited. (optional)
     * @param excludeLocationTypes Optional. If specified, results will be filtered based on the LocationType. This
     *            allows multiple, comma delimited. (optional)
     * @param isMissing Optional filter by items that are missing episodes or not. (optional)
     * @param isUnaired Optional filter by items that are unaired episodes or not. (optional)
     * @param minCommunityRating Optional filter by minimum community rating. (optional)
     * @param minCriticRating Optional filter by minimum critic rating. (optional)
     * @param minPremiereDate Optional. The minimum premiere date. Format &#x3D; ISO. (optional)
     * @param minDateLastSaved Optional. The minimum last saved date. Format &#x3D; ISO. (optional)
     * @param minDateLastSavedForUser Optional. The minimum last saved date for the current user. Format &#x3D; ISO.
     *            (optional)
     * @param maxPremiereDate Optional. The maximum premiere date. Format &#x3D; ISO. (optional)
     * @param hasOverview Optional filter by items that have an overview or not. (optional)
     * @param hasImdbId Optional filter by items that have an IMDb id or not. (optional)
     * @param hasTmdbId Optional filter by items that have a TMDb id or not. (optional)
     * @param hasTvdbId Optional filter by items that have a TVDb id or not. (optional)
     * @param isMovie Optional filter for live tv movies. (optional)
     * @param isSeries Optional filter for live tv series. (optional)
     * @param isNews Optional filter for live tv news. (optional)
     * @param isKids Optional filter for live tv kids. (optional)
     * @param isSports Optional filter for live tv sports. (optional)
     * @param excludeItemIds Optional. If specified, results will be filtered by excluding item ids. This allows
     *            multiple, comma delimited. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param recursive When searching within folders, this determines whether or not the search will be recursive.
     *            true/false. (optional)
     * @param searchTerm Optional. Filter based on a search term. (optional)
     * @param sortOrder Sort Order - Ascending, Descending. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines. (optional)
     * @param excludeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param filters Optional. Specify additional filters to apply. This allows multiple, comma delimited. Options:
     *            IsFolder, IsNotFolder, IsUnplayed, IsPlayed, IsFavorite, IsResumable, Likes, Dislikes. (optional)
     * @param isFavorite Optional filter by items that are marked as favorite, or not. (optional)
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited. (optional)
     * @param imageTypes Optional. If specified, results will be filtered based on those containing image types. This
     *            allows multiple, comma delimited. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param isPlayed Optional filter by items that are played, or not. (optional)
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe
     *            delimited. (optional)
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows
     *            multiple, pipe delimited. (optional)
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     *            (optional)
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple,
     *            comma delimited. (optional)
     * @param enableUserData Optional, include user data. (optional)
     * @param imageTypeLimit Optional, the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param person Optional. If specified, results will be filtered to include only those containing the specified
     *            person. (optional)
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified
     *            person id. (optional)
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those
     *            containing the specified person and PersonType. Allows multiple, comma-delimited. (optional)
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe
     *            delimited. (optional)
     * @param artists Optional. If specified, results will be filtered based on artists. This allows multiple, pipe
     *            delimited. (optional)
     * @param excludeArtistIds Optional. If specified, results will be filtered based on artist id. This allows
     *            multiple, pipe delimited. (optional)
     * @param artistIds Optional. If specified, results will be filtered to include only those containing the specified
     *            artist id. (optional)
     * @param albumArtistIds Optional. If specified, results will be filtered to include only those containing the
     *            specified album artist id. (optional)
     * @param contributingArtistIds Optional. If specified, results will be filtered to include only those containing
     *            the specified contributing artist id. (optional)
     * @param albums Optional. If specified, results will be filtered based on album. This allows multiple, pipe
     *            delimited. (optional)
     * @param albumIds Optional. If specified, results will be filtered based on album id. This allows multiple, pipe
     *            delimited. (optional)
     * @param ids Optional. If specific items are needed, specify a list of item id&#39;s to retrieve. This allows
     *            multiple, comma delimited. (optional)
     * @param videoTypes Optional filter by VideoType (videofile, dvd, bluray, iso). Allows multiple, comma delimited.
     *            (optional)
     * @param minOfficialRating Optional filter by minimum official rating (PG, PG-13, TV-MA, etc). (optional)
     * @param isLocked Optional filter by items that are locked. (optional)
     * @param isPlaceHolder Optional filter by items that are placeholders. (optional)
     * @param hasOfficialRating Optional filter by items that have official ratings. (optional)
     * @param collapseBoxSetItems Whether or not to hide items behind their boxsets. (optional)
     * @param minWidth Optional. Filter by the minimum width of the item. (optional)
     * @param minHeight Optional. Filter by the minimum height of the item. (optional)
     * @param maxWidth Optional. Filter by the maximum width of the item. (optional)
     * @param maxHeight Optional. Filter by the maximum height of the item. (optional)
     * @param is3D Optional filter by items that are 3D, or not. (optional)
     * @param seriesStatus Optional filter by Series Status. Allows multiple, comma delimited. (optional)
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string. (optional)
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string. (optional)
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     *            (optional)
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe
     *            delimited. (optional)
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe
     *            delimited. (optional)
     * @param enableTotalRecordCount Optional. Enable the total record count. (optional, default to true)
     * @param enableImages Optional, include image information in output. (optional, default to true)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Success</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public BaseItemDtoQueryResult getTrailers(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull String maxOfficialRating,
            @org.eclipse.jdt.annotation.NonNull Boolean hasThemeSong,
            @org.eclipse.jdt.annotation.NonNull Boolean hasThemeVideo,
            @org.eclipse.jdt.annotation.NonNull Boolean hasSubtitles,
            @org.eclipse.jdt.annotation.NonNull Boolean hasSpecialFeature,
            @org.eclipse.jdt.annotation.NonNull Boolean hasTrailer, @org.eclipse.jdt.annotation.NonNull UUID adjacentTo,
            @org.eclipse.jdt.annotation.NonNull Integer parentIndexNumber,
            @org.eclipse.jdt.annotation.NonNull Boolean hasParentalRating,
            @org.eclipse.jdt.annotation.NonNull Boolean isHd, @org.eclipse.jdt.annotation.NonNull Boolean is4K,
            @org.eclipse.jdt.annotation.NonNull List<LocationType> locationTypes,
            @org.eclipse.jdt.annotation.NonNull List<LocationType> excludeLocationTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean isMissing,
            @org.eclipse.jdt.annotation.NonNull Boolean isUnaired,
            @org.eclipse.jdt.annotation.NonNull Double minCommunityRating,
            @org.eclipse.jdt.annotation.NonNull Double minCriticRating,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime minPremiereDate,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime minDateLastSaved,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime minDateLastSavedForUser,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime maxPremiereDate,
            @org.eclipse.jdt.annotation.NonNull Boolean hasOverview,
            @org.eclipse.jdt.annotation.NonNull Boolean hasImdbId,
            @org.eclipse.jdt.annotation.NonNull Boolean hasTmdbId,
            @org.eclipse.jdt.annotation.NonNull Boolean hasTvdbId, @org.eclipse.jdt.annotation.NonNull Boolean isMovie,
            @org.eclipse.jdt.annotation.NonNull Boolean isSeries, @org.eclipse.jdt.annotation.NonNull Boolean isNews,
            @org.eclipse.jdt.annotation.NonNull Boolean isKids, @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeItemIds,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull Boolean recursive,
            @org.eclipse.jdt.annotation.NonNull String searchTerm,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> imageTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull Boolean isPlayed,
            @org.eclipse.jdt.annotation.NonNull List<String> genres,
            @org.eclipse.jdt.annotation.NonNull List<String> officialRatings,
            @org.eclipse.jdt.annotation.NonNull List<String> tags,
            @org.eclipse.jdt.annotation.NonNull List<Integer> years,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull String person, @org.eclipse.jdt.annotation.NonNull List<UUID> personIds,
            @org.eclipse.jdt.annotation.NonNull List<String> personTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> studios,
            @org.eclipse.jdt.annotation.NonNull List<String> artists,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull List<UUID> artistIds,
            @org.eclipse.jdt.annotation.NonNull List<UUID> albumArtistIds,
            @org.eclipse.jdt.annotation.NonNull List<UUID> contributingArtistIds,
            @org.eclipse.jdt.annotation.NonNull List<String> albums,
            @org.eclipse.jdt.annotation.NonNull List<UUID> albumIds, @org.eclipse.jdt.annotation.NonNull List<UUID> ids,
            @org.eclipse.jdt.annotation.NonNull List<VideoType> videoTypes,
            @org.eclipse.jdt.annotation.NonNull String minOfficialRating,
            @org.eclipse.jdt.annotation.NonNull Boolean isLocked,
            @org.eclipse.jdt.annotation.NonNull Boolean isPlaceHolder,
            @org.eclipse.jdt.annotation.NonNull Boolean hasOfficialRating,
            @org.eclipse.jdt.annotation.NonNull Boolean collapseBoxSetItems,
            @org.eclipse.jdt.annotation.NonNull Integer minWidth, @org.eclipse.jdt.annotation.NonNull Integer minHeight,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Boolean is3D,
            @org.eclipse.jdt.annotation.NonNull List<SeriesStatus> seriesStatus,
            @org.eclipse.jdt.annotation.NonNull String nameStartsWithOrGreater,
            @org.eclipse.jdt.annotation.NonNull String nameStartsWith,
            @org.eclipse.jdt.annotation.NonNull String nameLessThan,
            @org.eclipse.jdt.annotation.NonNull List<UUID> studioIds,
            @org.eclipse.jdt.annotation.NonNull List<UUID> genreIds,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages) throws ApiException {
        return getTrailersWithHttpInfo(userId, maxOfficialRating, hasThemeSong, hasThemeVideo, hasSubtitles,
                hasSpecialFeature, hasTrailer, adjacentTo, parentIndexNumber, hasParentalRating, isHd, is4K,
                locationTypes, excludeLocationTypes, isMissing, isUnaired, minCommunityRating, minCriticRating,
                minPremiereDate, minDateLastSaved, minDateLastSavedForUser, maxPremiereDate, hasOverview, hasImdbId,
                hasTmdbId, hasTvdbId, isMovie, isSeries, isNews, isKids, isSports, excludeItemIds, startIndex, limit,
                recursive, searchTerm, sortOrder, parentId, fields, excludeItemTypes, filters, isFavorite, mediaTypes,
                imageTypes, sortBy, isPlayed, genres, officialRatings, tags, years, enableUserData, imageTypeLimit,
                enableImageTypes, person, personIds, personTypes, studios, artists, excludeArtistIds, artistIds,
                albumArtistIds, contributingArtistIds, albums, albumIds, ids, videoTypes, minOfficialRating, isLocked,
                isPlaceHolder, hasOfficialRating, collapseBoxSetItems, minWidth, minHeight, maxWidth, maxHeight, is3D,
                seriesStatus, nameStartsWithOrGreater, nameStartsWith, nameLessThan, studioIds, genreIds,
                enableTotalRecordCount, enableImages).getData();
    }

    /**
     * Finds movies and trailers similar to a given trailer.
     * 
     * @param userId The user id supplied as query parameter; this is required when not using an API key. (optional)
     * @param maxOfficialRating Optional filter by maximum official rating (PG, PG-13, TV-MA, etc). (optional)
     * @param hasThemeSong Optional filter by items with theme songs. (optional)
     * @param hasThemeVideo Optional filter by items with theme videos. (optional)
     * @param hasSubtitles Optional filter by items with subtitles. (optional)
     * @param hasSpecialFeature Optional filter by items with special features. (optional)
     * @param hasTrailer Optional filter by items with trailers. (optional)
     * @param adjacentTo Optional. Return items that are siblings of a supplied item. (optional)
     * @param parentIndexNumber Optional filter by parent index number. (optional)
     * @param hasParentalRating Optional filter by items that have or do not have a parental rating. (optional)
     * @param isHd Optional filter by items that are HD or not. (optional)
     * @param is4K Optional filter by items that are 4K or not. (optional)
     * @param locationTypes Optional. If specified, results will be filtered based on LocationType. This allows
     *            multiple, comma delimited. (optional)
     * @param excludeLocationTypes Optional. If specified, results will be filtered based on the LocationType. This
     *            allows multiple, comma delimited. (optional)
     * @param isMissing Optional filter by items that are missing episodes or not. (optional)
     * @param isUnaired Optional filter by items that are unaired episodes or not. (optional)
     * @param minCommunityRating Optional filter by minimum community rating. (optional)
     * @param minCriticRating Optional filter by minimum critic rating. (optional)
     * @param minPremiereDate Optional. The minimum premiere date. Format &#x3D; ISO. (optional)
     * @param minDateLastSaved Optional. The minimum last saved date. Format &#x3D; ISO. (optional)
     * @param minDateLastSavedForUser Optional. The minimum last saved date for the current user. Format &#x3D; ISO.
     *            (optional)
     * @param maxPremiereDate Optional. The maximum premiere date. Format &#x3D; ISO. (optional)
     * @param hasOverview Optional filter by items that have an overview or not. (optional)
     * @param hasImdbId Optional filter by items that have an IMDb id or not. (optional)
     * @param hasTmdbId Optional filter by items that have a TMDb id or not. (optional)
     * @param hasTvdbId Optional filter by items that have a TVDb id or not. (optional)
     * @param isMovie Optional filter for live tv movies. (optional)
     * @param isSeries Optional filter for live tv series. (optional)
     * @param isNews Optional filter for live tv news. (optional)
     * @param isKids Optional filter for live tv kids. (optional)
     * @param isSports Optional filter for live tv sports. (optional)
     * @param excludeItemIds Optional. If specified, results will be filtered by excluding item ids. This allows
     *            multiple, comma delimited. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param recursive When searching within folders, this determines whether or not the search will be recursive.
     *            true/false. (optional)
     * @param searchTerm Optional. Filter based on a search term. (optional)
     * @param sortOrder Sort Order - Ascending, Descending. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines. (optional)
     * @param excludeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param filters Optional. Specify additional filters to apply. This allows multiple, comma delimited. Options:
     *            IsFolder, IsNotFolder, IsUnplayed, IsPlayed, IsFavorite, IsResumable, Likes, Dislikes. (optional)
     * @param isFavorite Optional filter by items that are marked as favorite, or not. (optional)
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited. (optional)
     * @param imageTypes Optional. If specified, results will be filtered based on those containing image types. This
     *            allows multiple, comma delimited. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param isPlayed Optional filter by items that are played, or not. (optional)
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe
     *            delimited. (optional)
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows
     *            multiple, pipe delimited. (optional)
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     *            (optional)
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple,
     *            comma delimited. (optional)
     * @param enableUserData Optional, include user data. (optional)
     * @param imageTypeLimit Optional, the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param person Optional. If specified, results will be filtered to include only those containing the specified
     *            person. (optional)
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified
     *            person id. (optional)
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those
     *            containing the specified person and PersonType. Allows multiple, comma-delimited. (optional)
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe
     *            delimited. (optional)
     * @param artists Optional. If specified, results will be filtered based on artists. This allows multiple, pipe
     *            delimited. (optional)
     * @param excludeArtistIds Optional. If specified, results will be filtered based on artist id. This allows
     *            multiple, pipe delimited. (optional)
     * @param artistIds Optional. If specified, results will be filtered to include only those containing the specified
     *            artist id. (optional)
     * @param albumArtistIds Optional. If specified, results will be filtered to include only those containing the
     *            specified album artist id. (optional)
     * @param contributingArtistIds Optional. If specified, results will be filtered to include only those containing
     *            the specified contributing artist id. (optional)
     * @param albums Optional. If specified, results will be filtered based on album. This allows multiple, pipe
     *            delimited. (optional)
     * @param albumIds Optional. If specified, results will be filtered based on album id. This allows multiple, pipe
     *            delimited. (optional)
     * @param ids Optional. If specific items are needed, specify a list of item id&#39;s to retrieve. This allows
     *            multiple, comma delimited. (optional)
     * @param videoTypes Optional filter by VideoType (videofile, dvd, bluray, iso). Allows multiple, comma delimited.
     *            (optional)
     * @param minOfficialRating Optional filter by minimum official rating (PG, PG-13, TV-MA, etc). (optional)
     * @param isLocked Optional filter by items that are locked. (optional)
     * @param isPlaceHolder Optional filter by items that are placeholders. (optional)
     * @param hasOfficialRating Optional filter by items that have official ratings. (optional)
     * @param collapseBoxSetItems Whether or not to hide items behind their boxsets. (optional)
     * @param minWidth Optional. Filter by the minimum width of the item. (optional)
     * @param minHeight Optional. Filter by the minimum height of the item. (optional)
     * @param maxWidth Optional. Filter by the maximum width of the item. (optional)
     * @param maxHeight Optional. Filter by the maximum height of the item. (optional)
     * @param is3D Optional filter by items that are 3D, or not. (optional)
     * @param seriesStatus Optional filter by Series Status. Allows multiple, comma delimited. (optional)
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string. (optional)
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string. (optional)
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     *            (optional)
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe
     *            delimited. (optional)
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe
     *            delimited. (optional)
     * @param enableTotalRecordCount Optional. Enable the total record count. (optional, default to true)
     * @param enableImages Optional, include image information in output. (optional, default to true)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Success</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<BaseItemDtoQueryResult> getTrailersWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull String maxOfficialRating,
            @org.eclipse.jdt.annotation.NonNull Boolean hasThemeSong,
            @org.eclipse.jdt.annotation.NonNull Boolean hasThemeVideo,
            @org.eclipse.jdt.annotation.NonNull Boolean hasSubtitles,
            @org.eclipse.jdt.annotation.NonNull Boolean hasSpecialFeature,
            @org.eclipse.jdt.annotation.NonNull Boolean hasTrailer, @org.eclipse.jdt.annotation.NonNull UUID adjacentTo,
            @org.eclipse.jdt.annotation.NonNull Integer parentIndexNumber,
            @org.eclipse.jdt.annotation.NonNull Boolean hasParentalRating,
            @org.eclipse.jdt.annotation.NonNull Boolean isHd, @org.eclipse.jdt.annotation.NonNull Boolean is4K,
            @org.eclipse.jdt.annotation.NonNull List<LocationType> locationTypes,
            @org.eclipse.jdt.annotation.NonNull List<LocationType> excludeLocationTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean isMissing,
            @org.eclipse.jdt.annotation.NonNull Boolean isUnaired,
            @org.eclipse.jdt.annotation.NonNull Double minCommunityRating,
            @org.eclipse.jdt.annotation.NonNull Double minCriticRating,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime minPremiereDate,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime minDateLastSaved,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime minDateLastSavedForUser,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime maxPremiereDate,
            @org.eclipse.jdt.annotation.NonNull Boolean hasOverview,
            @org.eclipse.jdt.annotation.NonNull Boolean hasImdbId,
            @org.eclipse.jdt.annotation.NonNull Boolean hasTmdbId,
            @org.eclipse.jdt.annotation.NonNull Boolean hasTvdbId, @org.eclipse.jdt.annotation.NonNull Boolean isMovie,
            @org.eclipse.jdt.annotation.NonNull Boolean isSeries, @org.eclipse.jdt.annotation.NonNull Boolean isNews,
            @org.eclipse.jdt.annotation.NonNull Boolean isKids, @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeItemIds,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull Boolean recursive,
            @org.eclipse.jdt.annotation.NonNull String searchTerm,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> imageTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull Boolean isPlayed,
            @org.eclipse.jdt.annotation.NonNull List<String> genres,
            @org.eclipse.jdt.annotation.NonNull List<String> officialRatings,
            @org.eclipse.jdt.annotation.NonNull List<String> tags,
            @org.eclipse.jdt.annotation.NonNull List<Integer> years,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull String person, @org.eclipse.jdt.annotation.NonNull List<UUID> personIds,
            @org.eclipse.jdt.annotation.NonNull List<String> personTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> studios,
            @org.eclipse.jdt.annotation.NonNull List<String> artists,
            @org.eclipse.jdt.annotation.NonNull List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.NonNull List<UUID> artistIds,
            @org.eclipse.jdt.annotation.NonNull List<UUID> albumArtistIds,
            @org.eclipse.jdt.annotation.NonNull List<UUID> contributingArtistIds,
            @org.eclipse.jdt.annotation.NonNull List<String> albums,
            @org.eclipse.jdt.annotation.NonNull List<UUID> albumIds, @org.eclipse.jdt.annotation.NonNull List<UUID> ids,
            @org.eclipse.jdt.annotation.NonNull List<VideoType> videoTypes,
            @org.eclipse.jdt.annotation.NonNull String minOfficialRating,
            @org.eclipse.jdt.annotation.NonNull Boolean isLocked,
            @org.eclipse.jdt.annotation.NonNull Boolean isPlaceHolder,
            @org.eclipse.jdt.annotation.NonNull Boolean hasOfficialRating,
            @org.eclipse.jdt.annotation.NonNull Boolean collapseBoxSetItems,
            @org.eclipse.jdt.annotation.NonNull Integer minWidth, @org.eclipse.jdt.annotation.NonNull Integer minHeight,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Boolean is3D,
            @org.eclipse.jdt.annotation.NonNull List<SeriesStatus> seriesStatus,
            @org.eclipse.jdt.annotation.NonNull String nameStartsWithOrGreater,
            @org.eclipse.jdt.annotation.NonNull String nameStartsWith,
            @org.eclipse.jdt.annotation.NonNull String nameLessThan,
            @org.eclipse.jdt.annotation.NonNull List<UUID> studioIds,
            @org.eclipse.jdt.annotation.NonNull List<UUID> genreIds,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxOfficialRating", maxOfficialRating));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "hasThemeSong", hasThemeSong));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "hasThemeVideo", hasThemeVideo));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "hasSubtitles", hasSubtitles));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "hasSpecialFeature", hasSpecialFeature));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "hasTrailer", hasTrailer));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "adjacentTo", adjacentTo));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "parentIndexNumber", parentIndexNumber));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "hasParentalRating", hasParentalRating));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isHd", isHd));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "is4K", is4K));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "locationTypes", locationTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "excludeLocationTypes", excludeLocationTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isMissing", isMissing));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isUnaired", isUnaired));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "minCommunityRating", minCommunityRating));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "minCriticRating", minCriticRating));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "minPremiereDate", minPremiereDate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "minDateLastSaved", minDateLastSaved));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "minDateLastSavedForUser", minDateLastSavedForUser));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxPremiereDate", maxPremiereDate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "hasOverview", hasOverview));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "hasImdbId", hasImdbId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "hasTmdbId", hasTmdbId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "hasTvdbId", hasTvdbId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isMovie", isMovie));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isSeries", isSeries));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isNews", isNews));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isKids", isKids));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isSports", isSports));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "excludeItemIds", excludeItemIds));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startIndex", startIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "recursive", recursive));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "searchTerm", searchTerm));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "sortOrder", sortOrder));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "parentId", parentId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "excludeItemTypes", excludeItemTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "filters", filters));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isFavorite", isFavorite));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "mediaTypes", mediaTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "imageTypes", imageTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "sortBy", sortBy));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isPlayed", isPlayed));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "genres", genres));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "officialRatings", officialRatings));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "tags", tags));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "years", years));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableUserData", enableUserData));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageTypeLimit", imageTypeLimit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "person", person));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "personIds", personIds));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "personTypes", personTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "studios", studios));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "artists", artists));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "excludeArtistIds", excludeArtistIds));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "artistIds", artistIds));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "albumArtistIds", albumArtistIds));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "contributingArtistIds", contributingArtistIds));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "albums", albums));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "albumIds", albumIds));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "ids", ids));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "videoTypes", videoTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "minOfficialRating", minOfficialRating));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isLocked", isLocked));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isPlaceHolder", isPlaceHolder));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "hasOfficialRating", hasOfficialRating));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "collapseBoxSetItems", collapseBoxSetItems));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "minWidth", minWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "minHeight", minHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "is3D", is3D));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "seriesStatus", seriesStatus));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "nameStartsWithOrGreater", nameStartsWithOrGreater));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "nameStartsWith", nameStartsWith));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "nameLessThan", nameLessThan));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "studioIds", studioIds));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "genreIds", genreIds));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableTotalRecordCount", enableTotalRecordCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableImages", enableImages));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDtoQueryResult> localVarReturnType = new GenericType<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("TrailersApi.getTrailers", "/Trailers", "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }
}
