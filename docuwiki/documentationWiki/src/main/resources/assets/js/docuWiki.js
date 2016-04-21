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
            	var res= element[0].style.height = "" + element[0].scrollHeight + "px";
            	return res;
            };
            $scope.resize=resize;
            $scope.focus=function() {
            	element.focus();
            }
            element.on("blur keyup change focus", resize);
        }
    };
});


docuWikiApp.directive("clickToEdit2", function($timeout) {

	return {
	    restrict: "A",
	    replace: true,
	    templateUrl: 'partials/editorTemplate2.html',
	    scope: {
	        value: "=clickToEdit2"
	    },
	    controller: function($scope) {
	        $scope.view = {
	            editableValue: $scope.value,
	            editorEnabled: false
	        };

	        $scope.enableEditor = function() {
	            if (!$scope.$parent.version){
		        	$scope.view.editorEnabled = true;
		        	if (!$scope.view.changed) {
		        		//the first time we edit
		        		//copy the original value so it can be reverted
			            $scope.origValue = angular.copy($scope.value);
		        	}
		        	$scope.view.changed = true;
		            $scope.$parent.edited.value=true;
	
		            $timeout($scope.focus, 0);
	            }
	        };
	        
	        $scope.closeEditor = function() {
	        	$scope.view.editorEnabled = false;
	        }

	        $scope.cancel = function() {
	            $scope.value = angular.copy($scope.origValue);
	            $scope.view.editorEnabled = false;
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
		topClass = topClass.replace("[]","");

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

docuWikiApp.filter('markupify', [function() {
	var substitutions = [{
			key:/<see cref=["']([a-zA-z.]*)["']\/>/g, //class link
			value:'<a href="#/classes/$1">$1</a>'
		},
		{
			key:/<see cref=["']([a-zA-z.]*)\.(\w*)\([a-zA-Z0-9, ]*\)["']\/>/g, //method link
			value:'<a href="#/classes/$1?scrollTo=$2M">$1.$2</a>'
		},
		{
			key:/<param name=["']([a-zA-z.]*)["']>([^<]*)<\/param>/g, 
			value:'<br>param $1 &ndash; $2'
		},
		{
			key:/<returns>([^<]*)<\/returns>/g, 
			value:'<br>returns &ndash; $1'
		},
		{
			key:/<typeparam name=["']([a-zA-z.]*)["']>([^<]*)<\/typeparam>/g, 
			value:'<br>typeparam $1 &ndash; $2'
		},
		{key:/<list type="bullet">/g, value:'<ul>'},
		{key:/<\/list>/g, value:'</ul>'},
		{key:/<item>/g, value:'<li>'},
		{key:/<\/item>/g, value:'</li>'},
		{key:/<para>/g, value:'<p>'},
		{key:/<\/para>/g, value:'</p>'},
		{key:/<c>/g, value:'<code>'},
		{key:/<\/c>/g, value:'</code>'},
		{key:/<warning>/g, value:'<em>'},
		{key:/<\/warning>/g, value:'</em>'}];

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
				$stateParams.scrollTo=null;
			},700);
		}
	});
});