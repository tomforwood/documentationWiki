var docuWikiApp = angular.module('docuWikiApp',['ngRoute','docuWikiServices']);

docuWikiApp.config(['$routeProvider',
    function($routeProvider){
		$routeProvider.when('/classList',{
			templateUrl:'partials/classList.html',
			controller:'classListCtrl'
		}).
		when('/classes/:classname', {
			templateUrl:'partials/classView.html',
			controller:'classViewCtrl'
		}).
		otherwise({redirectTo: '/classList'});
}]);


docuWikiApp.directive("clickToEdit", function() {

	return {
	    restrict: "A",
	    replace: true,
	    templateUrl: 'partials/editorTemplate.html',
	    scope: {
	        value: "=clickToEdit"
	    },
	    controller: function($scope) {
	        $scope.view = {
	            editableValue: $scope.value,
	            editorEnabled: false
	        };

	        $scope.enableEditor = function() {
	            $scope.view.editorEnabled = true;
	            $scope.view.editableValue = $scope.value;
	        };

	        $scope.disableEditor = function() {
	            $scope.view.editorEnabled = false;
	        };

	        $scope.save = function() {
	            $scope.value = $scope.view.editableValue;
	            $scope.disableEditor();
	        };
	    }
	};
	});