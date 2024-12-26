

# GetProgramsDto

Get programs dto.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**channelIds** | **List&lt;UUID&gt;** | Gets or sets the channels to return guide information for. |  [optional] |
|**userId** | **UUID** | Gets or sets optional. Filter by user id. |  [optional] |
|**minStartDate** | **OffsetDateTime** | Gets or sets the minimum premiere start date.  Optional. |  [optional] |
|**hasAired** | **Boolean** | Gets or sets filter by programs that have completed airing, or not.  Optional. |  [optional] |
|**isAiring** | **Boolean** | Gets or sets filter by programs that are currently airing, or not.  Optional. |  [optional] |
|**maxStartDate** | **OffsetDateTime** | Gets or sets the maximum premiere start date.  Optional. |  [optional] |
|**minEndDate** | **OffsetDateTime** | Gets or sets the minimum premiere end date.  Optional. |  [optional] |
|**maxEndDate** | **OffsetDateTime** | Gets or sets the maximum premiere end date.  Optional. |  [optional] |
|**isMovie** | **Boolean** | Gets or sets filter for movies.  Optional. |  [optional] |
|**isSeries** | **Boolean** | Gets or sets filter for series.  Optional. |  [optional] |
|**isNews** | **Boolean** | Gets or sets filter for news.  Optional. |  [optional] |
|**isKids** | **Boolean** | Gets or sets filter for kids.  Optional. |  [optional] |
|**isSports** | **Boolean** | Gets or sets filter for sports.  Optional. |  [optional] |
|**startIndex** | **Integer** | Gets or sets the record index to start at. All items with a lower index will be dropped from the results.  Optional. |  [optional] |
|**limit** | **Integer** | Gets or sets the maximum number of records to return.  Optional. |  [optional] |
|**sortBy** | **List&lt;String&gt;** | Gets or sets specify one or more sort orders, comma delimited. Options: Name, StartDate.  Optional. |  [optional] |
|**sortOrder** | **List&lt;SortOrder&gt;** | Gets or sets sort Order - Ascending,Descending. |  [optional] |
|**genres** | **List&lt;String&gt;** | Gets or sets the genres to return guide information for. |  [optional] |
|**genreIds** | **List&lt;UUID&gt;** | Gets or sets the genre ids to return guide information for. |  [optional] |
|**enableImages** | **Boolean** | Gets or sets include image information in output.  Optional. |  [optional] |
|**enableTotalRecordCount** | **Boolean** | Gets or sets a value indicating whether retrieve total record count. |  [optional] |
|**imageTypeLimit** | **Integer** | Gets or sets the max number of images to return, per image type.  Optional. |  [optional] |
|**enableImageTypes** | **List&lt;ImageType&gt;** | Gets or sets the image types to include in the output.  Optional. |  [optional] |
|**enableUserData** | **Boolean** | Gets or sets include user data.  Optional. |  [optional] |
|**seriesTimerId** | **String** | Gets or sets filter by series timer id.  Optional. |  [optional] |
|**librarySeriesId** | **UUID** | Gets or sets filter by library series id.  Optional. |  [optional] |
|**fields** | **List&lt;ItemFields&gt;** | Gets or sets specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines.  Optional. |  [optional] |



