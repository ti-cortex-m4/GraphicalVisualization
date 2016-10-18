(function() {

	angular.module("WebxenApp", [ 'ui.bootstrap' ]).directive("sunburst",
			sunburst).controller("ExposureController", ExposureController);

	/* Angular controller function ExposureController */
	function ExposureController($scope, $http) {
		var ctrl = this;
		var urlBase = "http://localhost:9090/WebxenBNY/app";

		$scope.generateCSV = function generateCSV() {

			$http.get(urlBase + "/test/generateCsv").success(
					function(response) {
					});

		};

		/*
		 * Function to send a request on submit of the form to backend and read
		 * the JSON response
		 */
		$scope.fetch = function fetch() {
			var myNode = document.getElementById("sunburst");
			while (myNode.firstChild) {
				myNode.removeChild(myNode.firstChild);
			}
			$('#loadingmessage').show();
			$http.get(
					urlBase + "/test/getExposure?submit=true&id=" + $scope.id
							+ "&type=" + $scope.type + "&typeExposure="
							+ $scope.typeExposure + "&typeHierarchy="
							+ $scope.typeHierarchy + "&date=" + $scope.date
							+ "&minExposure=" + $scope.minExposure
							+ "&maxExposure=" + $scope.maxExposure + "&number="
							+ $scope.number).success(function(response) {
				$scope.data = response.data;
				$('#loadingmessage').hide();
			}).then(function(response) {
				ctrl.data = $scope.data;
			});
		};

		/*
		 * Fucntion to send a request to backend on double click of any node and
		 * read the JSON response
		 */
		$scope.fetchOnClick = function fetchOnClick(elementId, typeHier) {
			var myNode = document.getElementById("sunburst");
			while (myNode.firstChild) {
				myNode.removeChild(myNode.firstChild);
			}
			$('#loadingmessage').show();
			$http.get(
					urlBase + "/test/getExposure?submit=false&id=" + elementId
							+ "&type=" + typeHier + "&typeExposure="
							+ $scope.typeExposure + "&typeHierarchy="
							+ $scope.typeHierarchy + "&date=" + $scope.date
							+ "&minExposure=" + $scope.minExposure
							+ "&maxExposure=" + $scope.maxExposure).success(
					function(response) {
						$scope.data = response.data;
						$('#loadingmessage').hide();
					}).then(function(response) {
				ctrl.data = $scope.data;
			});

		};

		/*
		 * Function for autocomplete which sends the input text as a request
		 * parameter and reads the JSON response
		 */
		$scope.names = [];
		$scope.$watch('id', function(val) {
			$http.get(
					urlBase + "/test/autocomplete?&id=" + val + "&type="
							+ $scope.type + "&typeExposure="
							+ $scope.typeExposure + "&typeHierarchy="
							+ $scope.typeHierarchy + "&date=" + $scope.date
							+ "&minExposure=" + $scope.minExposure
							+ "&maxExposure=" + $scope.maxExposure).then(
					function(res) {
						$scope.names.length = 0;
						angular.forEach(res.data.data, function(item) {
							$scope.names.push(item.name);
						});
					});
		});

		/*
		 * Function to send a request to backend on selection of a limiting
		 * number which is sent as a request parameter
		 */
		$scope.fetchOnNumber = function fetchOnNumber() {
			var myNode = document.getElementById("sunburst");
			while (myNode.firstChild) {
				myNode.removeChild(myNode.firstChild);
			}
			document.getElementById("errorRange").style.display = "none";
			$('#loadingmessage').show();
			$http.get(
					urlBase + "/test/getExposure?submit=false&id=" + $scope.id
							+ "&type=" + $scope.type + "&typeExposure="
							+ $scope.typeExposure + "&typeHierarchy="
							+ $scope.typeHierarchy + "&date=" + $scope.date
							+ "&minExposure=" + $scope.minExposure
							+ "&maxExposure=" + $scope.maxExposure + "&number="
							+ $scope.number).success(function(response) {
				$scope.data = response.data;
				$('#loadingmessage').hide();
			}).then(function(response) {
				ctrl.data = $scope.data;
			});

		};

	}

	/* directive function sunburst*/
	function sunburst() {
		return {
			restrict : "E",
			scope : {
				data : "=",
			},
			link : sunburstDraw
		};
	}

})();