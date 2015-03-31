angular.module('SmartHome.filters', []).filter 'activeItems', ->
	return (items) ->
		return null unless angular.isArray items
		items.filter (item) ->
			item.state isnt 'Uninitialized' and item.state isnt '0'