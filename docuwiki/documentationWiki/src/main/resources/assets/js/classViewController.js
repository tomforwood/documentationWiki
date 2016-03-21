/**
 * to load (and manipluate?) the merged class object
 */
//var classController = angular.module('docuWikiApp',['docuWikiServices']);

docuWikiApp.controller('classViewCtrl', ['$scope', 'Class', '$routeParams',
    function($scope, Class, $routeParams) {
		$scope.classFQN=$routeParams.classname
		$scope.mergedClass = Class.get({classid:$routeParams.classname}, function(classData){
					
				});
		
		$scope.save =  function() {Class.save($scope.mergedClass, function(){})};
			/*function(classname) {
			$http.post("/api/class/", $scope.mergedClass).then(function(response){
				$scope.PostDataResponse = response;
			}, function(errorresponse) {
				$scope.PostDataResponse = errorresponse;
			});
			return classname;
		}*/
    }]
);
