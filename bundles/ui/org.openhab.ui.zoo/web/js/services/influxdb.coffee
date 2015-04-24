'use strict'

angular.module('ZooLib.services.influxDb', []).factory 'influxDb', ($resource) ->

	host = '/zoo/influxproxy'

	$resource(host + '/db/:dbName/series?q=:query&u=:user&p=:password',
		dbName: 'openhab', user:'openhab', password:'openhab'
	)


