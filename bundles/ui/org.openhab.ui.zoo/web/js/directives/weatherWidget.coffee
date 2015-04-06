'use strict'

angular.module('ZooLib.directives.weatherWidget', []).directive 'weatherWidget', ->
	restrict: 'E'
	replace: yes
	templateUrl: 'partials/directives/weatherWidget.html'