var docuWikiServices = angular.module('docuWikiServices',['ngResource']);

docuWikiServices.factory('Class',['$resource',
    function($resource) {
		return $resource('api/class/:classid',{},{
			query : {method:'GET', params:{classid:'class'}, isArray:true}
		});
}]);

docuWikiServices.factory('ClassList', [ '$resource', function($resource) {
	return $resource('api/classList/', {}, {
		query : {
			method : 'GET',
			isArray : true
		}
	});
} ]);

docuWikiServices.factory('ClassUses', [ '$resource', function($resource) {
	return $resource('api/class/uses/:classid', {}, {
		query : {method:'GET', params:{classid:'class'}, isArray:false}
	});
} ]);