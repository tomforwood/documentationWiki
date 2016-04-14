/**
 * to load (and manipluate?) the merged class object
 */
//var classController = angular.module('docuWikiApp',['docuWikiServices']);

docuWikiApp.controller('classViewCtrl', ['$scope', 'Class', 'ClassList', 
                                         '$stateParams', '$state',
    function($scope, Class, ClassList, $stateParams, $state) {
		$scope.version = $stateParams.version;
		$scope.classFQN=$stateParams.classname
		var query = {classid:$stateParams.classname};
		if ($scope.version) {
			query.version=$scope.version;
		}
		$scope.mergedClass = Class.get(query, function(classData){
					
				});
		$scope.classList = ClassList.query(function(classList){});
		
		var saveParams = $scope.version?{action:"revert"}:{}
		$scope.save =  {
			buttonText:($scope.version?"Revert to version "+$scope.version:"Save"),
			confirm:(typeof $scope.version !== 'undefined'),
			action:function() {
				Class.save(saveParams,$scope.mergedClass, function(mergedClass){
					console.log(mergedClass.name);
					$state.go('classes.details.class',
							{classname:$stateParams.classname},
							{reload:true}
					)
				}, function(error) {
					console.log("an error");
					$scope.PostDataResponse = error;
				})
			}
		};
		
    }]
);

docuWikiApp.filter('objectType', ['$filter',function($filter) {
	return function(input, classList) {
		if (!input) return "";
		var out = $filter('classLinkFilter')(input.typeName,classList);
		if (input.varargs) {
			out+="<";
			for (var i=0;i<input.varargs.length;i++) {
				out+=$filter('objectType')(input.varargs[i],classList);
				if (i<input.varargs.length-1) out+=",";
			}
			out+=">"
		}

		return out;
	};
}]);

docuWikiApp.filter('inhertitedFilter', [function() {
	return function(input,typeEnding, classList) {
		var contains=false;
		var topClass = input.inheritedFrom;
		var dotPos= topClass.indexOf("+");
		if (dotPos>=0) {
			topClass = topClass.substr(0,dotPos);
		}
		
		for (i=0;i<classList.length;i++){
			if (classList[i].className==topClass)  {
				contains =true;
				break;
			}
		}
		
		if (contains) return '<a href="#/classes/'
				+topClass+'?scrollTo='+input.name+typeEnding+'">'+input.name+'</a>';
		return input.name;
	};
}]);

docuWikiApp.filter('methodFilter', ['$filter',function($filter) {
	return function(method, classList) {
		var methodSig = method.name+"(&#8203;"//invisible space as line break hint
		var i=0;
		for (i=0;i<method.parameters.length;i++) {
			if (i!=0) {
				methodSig+=", ";
			}
			methodSig+=$filter('objectType')(method.parameters[i].objectType, classList);
			first=false;
		}
		methodSig+=")";
		return methodSig;
	};
}]);
