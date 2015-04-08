'use strict'

angular.module('ZooLib.directives.usageTable', []).directive 'usageTable', (itemRepository) ->
	restrict: 'E'
	replace: yes
	templateUrl: 'partials/directives/usageTable.html'
	link: (scope, elem, attr) ->
		#itemRepository.getConsumptions
