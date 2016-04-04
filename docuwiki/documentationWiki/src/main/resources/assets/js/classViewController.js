/**
 * to load (and manipluate?) the merged class object
 */
//var classController = angular.module('docuWikiApp',['docuWikiServices']);

docuWikiApp.controller('classViewCtrl', ['$scope', 'Class', '$stateParams',
    function($scope, Class, $stateParams) {
		$scope.classFQN=$stateParams.classname
		$scope.mergedClass = Class.get({classid:$stateParams.classname}, function(classData){
					
				});
		
		$scope.save =  function() {Class.save($scope.mergedClass, function(mergedClass){
			console.log(mergedClass.name);
			$scope.mergedClass = Class.get({classid:mergedClass.name});
		}, function(error) {
			console.log("an error");
			$scope.PostDataResponse = error;
		})};
    }]
);

docuWikiApp.filter('objectType', ['$filter',function($filter) {
	return function(input) {
		if (!input) return "";
		var out = $filter('classLinkFilter')(input.typeName);
		if (input.varargs) {
			out+="<";
			for (var i=0;i<input.varargs.length;i++) {
				out+=$filter('objectType')(input.varargs[i]);
				if (i<input.varargs.length-1) out+=",";
			}
			out+=">"
		}

		return out;
	};
}]);

docuWikiApp.filter('inhertitedFilter', ['ClassList','$sce',function(ClassList, $sanitize) {
	var classList = ClassList.query();
	return function(input) {
		var contains=false;
		var topClass = input.inheritedFrom;
		var dotPos= topClass.indexOf(".");
		if (dotPos>=0) {
			topClass = topClass.substr(0,dotPos);
		}
		
		for (i=0;i<classList.length;i++){
			contains |=classList[i].className==topClass;
		}
		if (contains) return $sanitize.trustAsHtml('<a href="#/classes/'+topClass+'">'+input.name+'</a>');
		return input.name;
	};
}]);
