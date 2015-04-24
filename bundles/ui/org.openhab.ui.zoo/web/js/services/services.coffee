'use strict'

angular.module('SmartHome.services', [
	'SmartHome.services.datacache',
	'SmartHome.services.config',
	'SmartHome.services.rest',
	'SmartHome.services.repositories',
	'SmartHome.services.event'
])

angular.module('ZooLib.services', [
	'ZooLib.services.influxDb'
])