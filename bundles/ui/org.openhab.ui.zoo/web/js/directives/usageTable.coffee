'use strict'

angular.module('ZooLib.directives.usageTable', []).directive 'usageTable', (itemService, influxDb) ->
	restrict: 'E'
	replace: yes
	templateUrl: 'partials/directives/usageTable.html'
	link: (scope, elem, attr) ->

		itemService.getByName itemName:'gConsumptions', (data) ->
			globalConsumption: data.state
			rooms: data.members

		influx = influxDb.query query:'select difference(value) as diff from /^consum/i group by time(1d)', (data) ->
			console.log 'Influx response', influx
