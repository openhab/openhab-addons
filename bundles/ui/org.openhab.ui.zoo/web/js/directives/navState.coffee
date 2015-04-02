'use strict'

angular.module('ZooLib.directives.navState', ['ui.router']).directive 'navState', ($state) ->

	clearWatch = null

	link = (scope) ->
		scope.icon ?= scope.name
		scope.state ?= scope.name
		clearWatch = scope.$watch 'state', (val) ->
			if val then scope.linkToState = $state.href(val)
			clearWatch()

	restrict: 'E'
	replace: yes
	template: '<li ui-sref-active="active"><a href="{{linkToState}}"><i class="nav-icon i-{{icon}}"></i>{{title}}</a></li>'
	scope:
		name: '@'
		title: '@'
		icon: '@'
		state: '@'
	link: link

