'use strict'

angular.module('ZooLib.directives.usageTable', []).directive 'usageTable', ->
	restrict: 'E'
	replace: yes
	templateUrl: 'partials/directives/usageTable.html'