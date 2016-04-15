docuWikiApp.controller('classVersionsCtrl', ['$scope', 'ClassVersions', '$stateParams',
    function($scope, ClassVersions, $stateParams) {
		$scope.classFQN=$stateParams.classname
		$scope.classVersions = ClassVersions.query({classid:$stateParams.classname}, function(classData){					
		console.log($scope.classVersions)
		});
    }]
);