var docuWikiServices = angular.module('docuWikiServices',['ngResource']);

docuWikiServices.factory('Class',['$resource',
    function($resource) {
		return $resource('api/class/:classid',{},{
			get : {method:'GET', params:{classid:'class'}, isArray:false}
		});
}]);

docuWikiServices.factory('ClassList', [ '$resource', function($resource) {
	return $resource('api/classList/', {}, {
		query : {
			method : 'GET',
			isArray : true, 
			cache:true
		}
	});
} ]);

docuWikiServices.factory('ClassUses', [ '$resource', function($resource) {
	return $resource('api/class/uses/:classid', {}, {
		query : {method:'GET', params:{classid:'class'}, isArray:false}
	});
} ]);

docuWikiServices.factory('ClassVersions', [ '$resource', function($resource) {
	return $resource('api/versions/:classid', {}, {
		query : {method:'GET', params:{classid:'class'}, isArray:true}
	});
} ]);

docuWikiServices.factory('XMLFile', [ '$resource', function($resource) {
	return $resource('api/xml', {}, {
		query : {method:'GET', isArray:false},
		get : {method:'GET', params:{filename:'file'}}
	});
} ]);