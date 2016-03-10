'use strict'

angular.module('SmartHome.filters', [])

.filter 'activeItems', ->
	return (items, enabled) ->
		return items if enabled is false
		return null unless angular.isArray items
		items.filter (item) ->
			item.state isnt 'Uninitialized' and item.state isnt '0'

.filter 'masterSwitch', ->

	TAG_MASTER = 'master-switch'

	filterFn = (item) ->
		item.tags.indexOf(TAG_MASTER) >= 0

	return (items) ->
		return unless items

		# items can be an array of members or a group item
		groupItems = if items.members? then items.members else items

		groupItems.filter(filterFn)?[0]

.filter 'consumption', ->

	UNITS = ['kWh']
	UNINITIALIZED = 'Uninitialized'

	return (value, precision, uninitVal='-') ->
		return unless value
		return uninitVal if value is UNINITIALIZED
		valueNum = parseInt value, 10
		if valueNum >= 0
			"#{valueNum} #{UNITS[0]}"

.filter 'power', ->

	UNITS = ['kW']
	UNINITIALIZED = 'Uninitialized'

	return (value, precision, uninitVal='-') ->
		return unless value
		return uninitVal if value is UNINITIALIZED
		valueNum = parseInt value, 10
		if valueNum >= 0
			"#{valueNum} #{UNITS[0]}"