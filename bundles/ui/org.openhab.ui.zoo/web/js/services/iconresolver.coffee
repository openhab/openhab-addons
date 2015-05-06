'use strict'

angular.module('ZooLib.services.iconResolver', []).factory 'iconResolver', ->
	ICON_PREFIX = 'icon-'
	return (item) ->
		return unless item?.tags?
		return val for val in item.tags when val.substr(0, ICON_PREFIX.length) is ICON_PREFIX
