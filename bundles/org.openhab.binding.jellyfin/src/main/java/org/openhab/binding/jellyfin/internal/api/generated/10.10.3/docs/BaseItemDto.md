

# BaseItemDto

This is strictly used as a data transfer object from the api layer.  This holds information about a BaseItem in a format that is convenient for the client.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**name** | **String** | Gets or sets the name. |  [optional] |
|**originalTitle** | **String** |  |  [optional] |
|**serverId** | **String** | Gets or sets the server identifier. |  [optional] |
|**id** | **UUID** | Gets or sets the id. |  [optional] |
|**etag** | **String** | Gets or sets the etag. |  [optional] |
|**sourceType** | **String** | Gets or sets the type of the source. |  [optional] |
|**playlistItemId** | **String** | Gets or sets the playlist item identifier. |  [optional] |
|**dateCreated** | **OffsetDateTime** | Gets or sets the date created. |  [optional] |
|**dateLastMediaAdded** | **OffsetDateTime** |  |  [optional] |
|**extraType** | **ExtraType** |  |  [optional] |
|**airsBeforeSeasonNumber** | **Integer** |  |  [optional] |
|**airsAfterSeasonNumber** | **Integer** |  |  [optional] |
|**airsBeforeEpisodeNumber** | **Integer** |  |  [optional] |
|**canDelete** | **Boolean** |  |  [optional] |
|**canDownload** | **Boolean** |  |  [optional] |
|**hasLyrics** | **Boolean** |  |  [optional] |
|**hasSubtitles** | **Boolean** |  |  [optional] |
|**preferredMetadataLanguage** | **String** |  |  [optional] |
|**preferredMetadataCountryCode** | **String** |  |  [optional] |
|**container** | **String** |  |  [optional] |
|**sortName** | **String** | Gets or sets the name of the sort. |  [optional] |
|**forcedSortName** | **String** |  |  [optional] |
|**video3DFormat** | **Video3DFormat** | Gets or sets the video3 D format. |  [optional] |
|**premiereDate** | **OffsetDateTime** | Gets or sets the premiere date. |  [optional] |
|**externalUrls** | [**List&lt;ExternalUrl&gt;**](ExternalUrl.md) | Gets or sets the external urls. |  [optional] |
|**mediaSources** | [**List&lt;MediaSourceInfo&gt;**](MediaSourceInfo.md) | Gets or sets the media versions. |  [optional] |
|**criticRating** | **Float** | Gets or sets the critic rating. |  [optional] |
|**productionLocations** | **List&lt;String&gt;** |  |  [optional] |
|**path** | **String** | Gets or sets the path. |  [optional] |
|**enableMediaSourceDisplay** | **Boolean** |  |  [optional] |
|**officialRating** | **String** | Gets or sets the official rating. |  [optional] |
|**customRating** | **String** | Gets or sets the custom rating. |  [optional] |
|**channelId** | **UUID** | Gets or sets the channel identifier. |  [optional] |
|**channelName** | **String** |  |  [optional] |
|**overview** | **String** | Gets or sets the overview. |  [optional] |
|**taglines** | **List&lt;String&gt;** | Gets or sets the taglines. |  [optional] |
|**genres** | **List&lt;String&gt;** | Gets or sets the genres. |  [optional] |
|**communityRating** | **Float** | Gets or sets the community rating. |  [optional] |
|**cumulativeRunTimeTicks** | **Long** | Gets or sets the cumulative run time ticks. |  [optional] |
|**runTimeTicks** | **Long** | Gets or sets the run time ticks. |  [optional] |
|**playAccess** | **PlayAccess** | Gets or sets the play access. |  [optional] |
|**aspectRatio** | **String** | Gets or sets the aspect ratio. |  [optional] |
|**productionYear** | **Integer** | Gets or sets the production year. |  [optional] |
|**isPlaceHolder** | **Boolean** | Gets or sets a value indicating whether this instance is place holder. |  [optional] |
|**number** | **String** | Gets or sets the number. |  [optional] |
|**channelNumber** | **String** |  |  [optional] |
|**indexNumber** | **Integer** | Gets or sets the index number. |  [optional] |
|**indexNumberEnd** | **Integer** | Gets or sets the index number end. |  [optional] |
|**parentIndexNumber** | **Integer** | Gets or sets the parent index number. |  [optional] |
|**remoteTrailers** | [**List&lt;MediaUrl&gt;**](MediaUrl.md) | Gets or sets the trailer urls. |  [optional] |
|**providerIds** | **Map&lt;String, String&gt;** | Gets or sets the provider ids. |  [optional] |
|**isHD** | **Boolean** | Gets or sets a value indicating whether this instance is HD. |  [optional] |
|**isFolder** | **Boolean** | Gets or sets a value indicating whether this instance is folder. |  [optional] |
|**parentId** | **UUID** | Gets or sets the parent id. |  [optional] |
|**type** | **BaseItemKind** | The base item kind. |  [optional] |
|**people** | [**List&lt;BaseItemPerson&gt;**](BaseItemPerson.md) | Gets or sets the people. |  [optional] |
|**studios** | [**List&lt;NameGuidPair&gt;**](NameGuidPair.md) | Gets or sets the studios. |  [optional] |
|**genreItems** | [**List&lt;NameGuidPair&gt;**](NameGuidPair.md) |  |  [optional] |
|**parentLogoItemId** | **UUID** | Gets or sets whether the item has a logo, this will hold the Id of the Parent that has one. |  [optional] |
|**parentBackdropItemId** | **UUID** | Gets or sets whether the item has any backdrops, this will hold the Id of the Parent that has one. |  [optional] |
|**parentBackdropImageTags** | **List&lt;String&gt;** | Gets or sets the parent backdrop image tags. |  [optional] |
|**localTrailerCount** | **Integer** | Gets or sets the local trailer count. |  [optional] |
|**userData** | [**UserItemDataDto**](UserItemDataDto.md) | Gets or sets the user data for this item based on the user it&#39;s being requested for. |  [optional] |
|**recursiveItemCount** | **Integer** | Gets or sets the recursive item count. |  [optional] |
|**childCount** | **Integer** | Gets or sets the child count. |  [optional] |
|**seriesName** | **String** | Gets or sets the name of the series. |  [optional] |
|**seriesId** | **UUID** | Gets or sets the series id. |  [optional] |
|**seasonId** | **UUID** | Gets or sets the season identifier. |  [optional] |
|**specialFeatureCount** | **Integer** | Gets or sets the special feature count. |  [optional] |
|**displayPreferencesId** | **String** | Gets or sets the display preferences id. |  [optional] |
|**status** | **String** | Gets or sets the status. |  [optional] |
|**airTime** | **String** | Gets or sets the air time. |  [optional] |
|**airDays** | **List&lt;DayOfWeek&gt;** | Gets or sets the air days. |  [optional] |
|**tags** | **List&lt;String&gt;** | Gets or sets the tags. |  [optional] |
|**primaryImageAspectRatio** | **Double** | Gets or sets the primary image aspect ratio, after image enhancements. |  [optional] |
|**artists** | **List&lt;String&gt;** | Gets or sets the artists. |  [optional] |
|**artistItems** | [**List&lt;NameGuidPair&gt;**](NameGuidPair.md) | Gets or sets the artist items. |  [optional] |
|**album** | **String** | Gets or sets the album. |  [optional] |
|**collectionType** | **CollectionType** | Gets or sets the type of the collection. |  [optional] |
|**displayOrder** | **String** | Gets or sets the display order. |  [optional] |
|**albumId** | **UUID** | Gets or sets the album id. |  [optional] |
|**albumPrimaryImageTag** | **String** | Gets or sets the album image tag. |  [optional] |
|**seriesPrimaryImageTag** | **String** | Gets or sets the series primary image tag. |  [optional] |
|**albumArtist** | **String** | Gets or sets the album artist. |  [optional] |
|**albumArtists** | [**List&lt;NameGuidPair&gt;**](NameGuidPair.md) | Gets or sets the album artists. |  [optional] |
|**seasonName** | **String** | Gets or sets the name of the season. |  [optional] |
|**mediaStreams** | [**List&lt;MediaStream&gt;**](MediaStream.md) | Gets or sets the media streams. |  [optional] |
|**videoType** | **VideoType** | Gets or sets the type of the video. |  [optional] |
|**partCount** | **Integer** | Gets or sets the part count. |  [optional] |
|**mediaSourceCount** | **Integer** |  |  [optional] |
|**imageTags** | **Map&lt;String, String&gt;** | Gets or sets the image tags. |  [optional] |
|**backdropImageTags** | **List&lt;String&gt;** | Gets or sets the backdrop image tags. |  [optional] |
|**screenshotImageTags** | **List&lt;String&gt;** | Gets or sets the screenshot image tags. |  [optional] |
|**parentLogoImageTag** | **String** | Gets or sets the parent logo image tag. |  [optional] |
|**parentArtItemId** | **UUID** | Gets or sets whether the item has fan art, this will hold the Id of the Parent that has one. |  [optional] |
|**parentArtImageTag** | **String** | Gets or sets the parent art image tag. |  [optional] |
|**seriesThumbImageTag** | **String** | Gets or sets the series thumb image tag. |  [optional] |
|**imageBlurHashes** | [**BaseItemDtoImageBlurHashes**](BaseItemDtoImageBlurHashes.md) |  |  [optional] |
|**seriesStudio** | **String** | Gets or sets the series studio. |  [optional] |
|**parentThumbItemId** | **UUID** | Gets or sets the parent thumb item id. |  [optional] |
|**parentThumbImageTag** | **String** | Gets or sets the parent thumb image tag. |  [optional] |
|**parentPrimaryImageItemId** | **String** | Gets or sets the parent primary image item identifier. |  [optional] |
|**parentPrimaryImageTag** | **String** | Gets or sets the parent primary image tag. |  [optional] |
|**chapters** | [**List&lt;ChapterInfo&gt;**](ChapterInfo.md) | Gets or sets the chapters. |  [optional] |
|**trickplay** | **Map&lt;String, Map&lt;String, TrickplayInfo&gt;&gt;** | Gets or sets the trickplay manifest. |  [optional] |
|**locationType** | **LocationType** | Gets or sets the type of the location. |  [optional] |
|**isoType** | **IsoType** | Gets or sets the type of the iso. |  [optional] |
|**mediaType** | **MediaType** | Media types. |  [optional] |
|**endDate** | **OffsetDateTime** | Gets or sets the end date. |  [optional] |
|**lockedFields** | **List&lt;MetadataField&gt;** | Gets or sets the locked fields. |  [optional] |
|**trailerCount** | **Integer** | Gets or sets the trailer count. |  [optional] |
|**movieCount** | **Integer** | Gets or sets the movie count. |  [optional] |
|**seriesCount** | **Integer** | Gets or sets the series count. |  [optional] |
|**programCount** | **Integer** |  |  [optional] |
|**episodeCount** | **Integer** | Gets or sets the episode count. |  [optional] |
|**songCount** | **Integer** | Gets or sets the song count. |  [optional] |
|**albumCount** | **Integer** | Gets or sets the album count. |  [optional] |
|**artistCount** | **Integer** |  |  [optional] |
|**musicVideoCount** | **Integer** | Gets or sets the music video count. |  [optional] |
|**lockData** | **Boolean** | Gets or sets a value indicating whether [enable internet providers]. |  [optional] |
|**width** | **Integer** |  |  [optional] |
|**height** | **Integer** |  |  [optional] |
|**cameraMake** | **String** |  |  [optional] |
|**cameraModel** | **String** |  |  [optional] |
|**software** | **String** |  |  [optional] |
|**exposureTime** | **Double** |  |  [optional] |
|**focalLength** | **Double** |  |  [optional] |
|**imageOrientation** | **ImageOrientation** |  |  [optional] |
|**aperture** | **Double** |  |  [optional] |
|**shutterSpeed** | **Double** |  |  [optional] |
|**latitude** | **Double** |  |  [optional] |
|**longitude** | **Double** |  |  [optional] |
|**altitude** | **Double** |  |  [optional] |
|**isoSpeedRating** | **Integer** |  |  [optional] |
|**seriesTimerId** | **String** | Gets or sets the series timer identifier. |  [optional] |
|**programId** | **String** | Gets or sets the program identifier. |  [optional] |
|**channelPrimaryImageTag** | **String** | Gets or sets the channel primary image tag. |  [optional] |
|**startDate** | **OffsetDateTime** | Gets or sets the start date of the recording, in UTC. |  [optional] |
|**completionPercentage** | **Double** | Gets or sets the completion percentage. |  [optional] |
|**isRepeat** | **Boolean** | Gets or sets a value indicating whether this instance is repeat. |  [optional] |
|**episodeTitle** | **String** | Gets or sets the episode title. |  [optional] |
|**channelType** | **ChannelType** | Gets or sets the type of the channel. |  [optional] |
|**audio** | **ProgramAudio** | Gets or sets the audio. |  [optional] |
|**isMovie** | **Boolean** | Gets or sets a value indicating whether this instance is movie. |  [optional] |
|**isSports** | **Boolean** | Gets or sets a value indicating whether this instance is sports. |  [optional] |
|**isSeries** | **Boolean** | Gets or sets a value indicating whether this instance is series. |  [optional] |
|**isLive** | **Boolean** | Gets or sets a value indicating whether this instance is live. |  [optional] |
|**isNews** | **Boolean** | Gets or sets a value indicating whether this instance is news. |  [optional] |
|**isKids** | **Boolean** | Gets or sets a value indicating whether this instance is kids. |  [optional] |
|**isPremiere** | **Boolean** | Gets or sets a value indicating whether this instance is premiere. |  [optional] |
|**timerId** | **String** | Gets or sets the timer identifier. |  [optional] |
|**normalizationGain** | **Float** | Gets or sets the gain required for audio normalization. |  [optional] |
|**currentProgram** | [**BaseItemDto**](BaseItemDto.md) | Gets or sets the current program. |  [optional] |



