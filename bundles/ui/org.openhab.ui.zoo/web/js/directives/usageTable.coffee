'use strict'

angular.module('ZooLib.directives.usageTable', []).directive 'usageTable', (itemService) ->
	restrict: 'E'
	replace: yes
	templateUrl: 'partials/directives/usageTable.html'
	link: (scope, elem, attr) ->

		itemService.getByName itemName:'gConsumptions', (data) ->
			scope.data =
				globalConsumption: data.state
				rooms: data.members

