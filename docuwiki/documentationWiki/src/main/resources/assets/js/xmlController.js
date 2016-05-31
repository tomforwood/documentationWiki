docuWikiApp.controller('xmlCtrl', ['$scope', 'XMLFile',
    function($scope, XMLFile) {
		$scope.fileList = XMLFile.query();
		console.log($scope.fileURL);
	}                               
]);
