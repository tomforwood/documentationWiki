var docuWikiApp = angular.module('docuWikiApp',['ngSanitize','docuWikiServices','ui.router']);

docuWikiApp.config(function($stateProvider, $urlRouterProvider){
	$urlRouterProvider.otherwise('/about');	
	
	$stateProvider
		.state('about',{
			url: '/about',
			templateUrl: 'partials/about.html'
		})
		.state('classes',{
			url: '/classes',
			templateUrl: 'partials/classes.html'
		})
		.state('classes.details',{
			url: '/:classname',
			templateUrl: 'partials/classTop.html',
			abstract: true				
		})
		.state('classes.details.class',{
			url: '',
			templateUrl: 'partials/classDetails.html',
			controller: 'classViewCtrl'
		})
		.state('classes.details.uses',{
			url: '/:classname/uses',
			templateUrl: 'partials/classUses.html',
			controller: 'classUsesCtrl'
		})
});


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

docuWikiApp.filter('classLinkFilter', ['ClassList','$sce',function(ClassList, $sanitize) {
	var classList = ClassList.query();
	return function(input) {
		var contains=false;
		//Remove subclasses e.g Administation+StrategyWrapper from the lookup
		var topClass = input;
		var plusPos= input.indexOf("+");
		if (plusPos>=0) {
			topClass = input.substr(0,plusPos);
		}
		
		for (i=0;i<classList.length;i++){
			contains |=classList[i].className==topClass;
		}
		if (contains) return $sanitize.trustAsHtml('<a href="#/classes/'+topClass+'">'+input.replace("+",".")+'</a>');
		return input.split(".").pop();
	};
}]);