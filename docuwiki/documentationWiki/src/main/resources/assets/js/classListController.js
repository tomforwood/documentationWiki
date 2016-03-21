docuWikiApp.controller('classListCtrl', ['$scope', 'ClassList', function($scope, ClassList){
	$scope.classes =ClassList.query();
}]);

docuWikiApp.filter('subset', function() {
	return function(classDefs, subset) {
		var filtered = [];
		angular.forEach(classDefs, function(item) {
			switch (subset) {
			case "ALL":
				filtered.push(item);
				break;
			case "DOCUMENTED":
				if (item.subset&2) filtered.push(item)
				break;
			case "ORPHANED":
				if (!(item.subset&1)) filtered.push(item)
				break;
			default:
			}
		});
		return filtered;
	};
});