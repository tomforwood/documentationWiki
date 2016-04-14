var docuWikiApp = angular.module('docuWikiApp',['angular-confirm', 'ui.bootstrap.tpls', 'ngSanitize','docuWikiServices','ui.router','ui.router.state']);


docuWikiApp.config(function($stateProvider, $urlRouterProvider, $uiViewScrollProvider){
	$urlRouterProvider.otherwise('/about');	
	$uiViewScrollProvider.useAnchorScroll();
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
			url: '/:classname?scrollTo&version',
			templateUrl: 'partials/classTop.html',
			abstract: true				
		})
		.state('classes.details.class',{
			url: '',
			templateUrl: 'partials/classDetails.html',
			controller: 'classViewCtrl'
		})
		.state('classes.details.uses',{
			url: '/uses',
			templateUrl: 'partials/classUses.html',
			controller: 'classUsesCtrl'
		})
		.state('classes.details.versions',{
			url: '/versions',
			templateUrl: 'partials/classVersions.html',
			controller: 'classVersionsCtrl'
		})
});

docuWikiApp.directive("autoHeight", function ($timeout) {
    return {
        restrict: "EAC",
        link: function($scope, element) {
            if(element[0].scrollHeight < 30){
                element[0].style.height = 30;
            }else{
                element[0].style.height = (element[0].scrollHeight) + "px";
            }

            var resize = function() {
              return element[0].style.height = "" + element[0].scrollHeight + "px";
            };
            $scope.resize=resize;

            element.on("blur keyup change", resize);
            $timeout(resize, 0)
        }
    };
});


docuWikiApp.directive("clickToEdit", function($timeout) {

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
	            $timeout($scope.resize, 0);
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

docuWikiApp.filter('classLinkFilter', ['$sce',function($sanitize) {
	return function(input, classList) {
		var contains=false;
		//Remove subclasses e.g Administation+StrategyWrapper from the lookup
		var topClass = input;
		var plusPos= input.indexOf("+");
		var nestedScroll="";
		if (plusPos>=0) {
			topClass = input.substr(0,plusPos);
			nestedScroll = "?scrollTo="+input.substr(plusPos+1)+"N";
		}

		for (i=0;i<classList.length;i++){
			if (classList[i].className==topClass)  {
				contains =true;
				break;
			}
		}

		if (contains) {
			return '<a href="#/classes/'
					+topClass+nestedScroll+'">'+input.replace("+",".")+'</a>';
		}
		return input.split(".").pop();
	};
}]);

docuWikiApp.filter('markupify', ['$sce',function($sanitize) {
	var substitutions = [{
		key:/<see cref="([a-zA-z.]*)"\/>/, 
		value:'<a href="#/classes/$1">$1</a>'}];

	return function(input) {
		if (!input) return input;
		for (var i=0;i<substitutions.length;i++){
			var entry = substitutions[i];
			input=input.replace(entry.key,entry.value);
		}
		return input;
	};
}]);

docuWikiApp.run(function($rootScope, $location, $anchorScroll, $stateParams, $timeout) {
	$rootScope.$on('$viewContentLoaded', function(newRoute, oldRoute) {
		if ($stateParams.scrollTo) {
			$timeout(function(){
				$anchorScroll($stateParams.scrollTo);  
			},100);
		}
	});
});