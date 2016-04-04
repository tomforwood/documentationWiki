docuWikiApp.controller('classUsesCtrl', ['$scope', 'ClassUses', '$stateParams',
    function($scope, ClassUses, $stateParams) {
		$scope.classFQN=$stateParams.classname
		$scope.classUses = ClassUses.get({classid:$stateParams.classname}, function(classData){
					
				});
    }]
);