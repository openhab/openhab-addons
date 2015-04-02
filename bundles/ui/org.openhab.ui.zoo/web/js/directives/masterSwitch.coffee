'use strict'

angular.module('ZooLib.directives.masterSwitch', []).directive 'masterSwitch', ->

	restrict: 'E'
	replace: yes
	templateUrl: 'partials/directives/masterSwitch.html'
	scope:
		item: '='

