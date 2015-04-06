'use strict'

angular.module('SmartHome.filters', [])

.filter 'activeItems', ->
	return (items, enabled) ->
		return items if enabled is false
		return null unless angular.isArray items
		items.filter (item) ->
			item.state isnt 'Uninitialized' and item.state isnt '0'

.filter 'masterSwitch', ->

	filterFn = (item) ->
		item.tags.indexOf('master') >= 0

	return (items) ->
		return unless items

		# items can be an array of members or a group item
		groupItems = if items.members? then items.members else items

		groupItems.filter(filterFn)?[0]