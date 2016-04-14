docuWikiApp.controller('classUsesCtrl', ['$scope', 'ClassUses', '$stateParams',
    function($scope, ClassUses, $stateParams) {
		$scope.classFQN=$stateParams.classname
		$scope.classUses = ClassUses.query({classid:$stateParams.classname}, function(classData){					
		});
    }]
);