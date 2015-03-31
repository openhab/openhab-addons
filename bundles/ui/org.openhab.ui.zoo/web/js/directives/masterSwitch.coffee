angular.module('ZooLib.directives.MasterSwitch', []).directive 'masterSwitch', ->

	restrict: 'E'
	replace: yes
	templateUrl: 'partials/directives/masterSwitch.html'
	scope:
		value: '='

