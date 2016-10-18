var force;
var svg;
var path;
var node;
var scale;
var scaleDuration;
var zoom;
var width, height;
var radiusType;
var padding; // separation between circles
var radius;
var quadtree;

function sunburstDraw(scope, element) {
	width = 900;
	height = 600;
	padding = 1; // separation between circles
	radius=8;

	/**
	 * Angular variables
	 * 
	 */
	// watch for changes on scope.data
	scope.$watch("data", function() {
		var data = scope.data;
		console.log("Inside scope");
		console.log("scope.data= " + scope.data);
		console.log("Data " + data);
		if(data.length!=0) {
			render(data);
		} else {
			document.getElementById("errorRange").innerHTML="Sorry, your search return zero results. Please modify the search criteria and try again.";
			document.getElementById("errorRange").style.display="block";
		}
	});

	var margin = {
		top : 20,
		right : 80,
		bottom : 30,
		left : 50
	};
	// Node dimensions
	var tocolor = "fill";
	var towhite = "stroke";
	var nominal_stroke = 1.5;

	var offsetX;
	var offsetY;

	var minExposure;
	var maxExposure;
	

	/**
	 * Render process:
	 */
	// render visualization
	function render(data) {
		// load data
		var json = data;
		console.log("JSON Object " + json);
		drawSunburst(json);
	}

	function drawSunburst(links) { // Start json parsing
		var nodes = {};

		// for auto-complete search box
		var optArray = [];

		// Compute the distinct nodes from the links. Elements in "d" are set from this loop
		links.forEach(function(link) {
			link.source = nodes[link.source.id] || (nodes[link.source.id] = {
				name : link.source.name,
				sKey : link.source.sKey,
				id : link.source.id,
				type : link.source.type,
				exposureAmount : link.source.exposureAmount,
				exposureDuration : link.source.exposureDuration,
				color : link.source.color
			});
			link.target = nodes[link.target.id] || (nodes[link.target.id] = {
				name : link.target.name,
				sKey : link.target.sKey,
				id : link.target.id,
				type : link.target.type,
				exposureAmount : link.target.exposureAmount,
				exposureDuration : link.target.exposureDuration,
				color : link.target.color
			});

			//Important to know the minumum and maximum exposureAmount and durationAmount, min and max values are passed to scale function
			if (minExposure == null || minExposure == undefined
					|| minExposure > link.source.exposureAmount
					|| minExposure > link.target.exposureAmount) {
				minExposure = Math.min(link.source.exposureAmount,
						link.target.exposureAmount)
			}
			if (maxExposure == null || maxExposure == undefined
					|| maxExposure < link.source.exposureAmount
					|| maxExposure < link.target.exposureAmount) {
				maxExposure = Math.max(link.source.exposureAmount,
						link.target.exposureAmount)
			}

		});

		zoom = d3.behavior.zoom().scaleExtent([ 0.4, 3 ]).on(
				"zoom",
				function() {
					svg.attr("transform", "translate(" + d3.event.translate
							+ ")scale(" + d3.event.scale + ")");
				});

		//Main configuration for force graph.
		force = d3.layout.force().nodes(d3.values(nodes)).links(links)
				.size([ width, height ]).linkDistance(70).charge(-800).gravity(0.2).on(
						"tick", tick).start();

		svg = d3.select(element[0]).append("svg").attr("width", "100%").attr(
				"height", height).call(zoom).append('svg:g');

		// build the arrow.
		svg.append("svg:defs").append("svg:marker").attr("id", "arrow").attr(
				"viewBox", "0 0 10 10").attr("refX", 10).attr("refY", 5).attr(
				"markerUnits", "strokeWidth").attr("markerWidth", 8).attr(
				"markerHeight", 6).attr("orient", "auto").append("svg:path")
				.attr("d", "M 0 0 L 10 5 L 0 10 z");

		// add the links and the arrows
		path = svg.append("svg:g").selectAll("path").data(force.links())
				.enter().append("svg:path").attr("class", "link").attr(
						"marker-end", "url(#arrow)");

		// define the nodes
		node = svg.selectAll(".node").data(force.nodes()).enter().append(
				"g").attr("class", "node").on("click", connectedNodes).call(
				force.drag) // force drag position is important
		.on("mousedown", function() {
			d3.event.stopPropagation();
		})
		.on(
				"mouseover",
				function(d) {

					// Update the tooltip position and value
					d3.select("#tooltip").style("left", 34 + "px").style("top",
							90 + "px").select("#name").text(d.name);
					// + " " + "$ " + d3 .format(",")(d.exposureAmount)
					// + " " + d.exposureDuration
					d3.select("#tooltip").select('#type').text(d.type);
					d3.select("#tooltip").select('#exposureAmount').text(
							"$ " + d3.format(",")(Math.abs(d.exposureAmount)));
					d3.select("#tooltip").select('#exposureDuration').text(
							("0" + Math.floor((d.exposureDuration) / 60))
									.slice(-2)
									+ ":"
									+ ("0" + (d.exposureDuration) % 60)
											.slice(-2) + " hours");

					// Show the tooltip
					d3.select("#tooltip").classed("hidden", false);

				}).on("mouseout", function() {
			// Hide the tooltip
			d3.select("#tooltip").classed("hidden", true);

		}).on("dblclick", dblclick);

		//Scaling by amount
		scale = d3.scale.linear().domain([ maxExposure, minExposure ])
				.range([ 5, 30 ]);

		//Scaling by duration
		scaleDuration = d3.scale.linear().domain([ 10, 1440 ]).range(
				[ 2, 36 ]);
		
		//quadtree is used to avoid overlapping of nodes.
		quadtree = d3.geom.quadtree(d3.values(nodes));
		
		node.append("circle").attr(tocolor, function(d) {
			// alert(JSON.stringify(d));
			return d.color;
		}).style("stroke-width", nominal_stroke).style(towhite, "white").attr(
				"r", function(d) {
					// alert(JSON.stringify(scale(d.exposureAmount)));
					if (d.exposureAmount == null)
						return 5;
					else
						return scale(d.exposureAmount);
				}).attr("data-legend", function(d) {
					
					switch(d.type) {
					case "RiskLegalEntity" :
						return "Risk Legal Entity";
						break;
					case "LegalEntity" :
						return "Legal Entity";
						break;
					case "LOB" :
						return "Line of Business";
						break;
					case "InvolvedParty" :
						return "Involved Party";
						break;
					case "CountryOfRisk" :
						return "Country of Risk";
						break;
					default :
						return d.type;
						break;
						
					}
		})

		// add the text
		node.append("text").attr("x", 12).attr("dy", "0.35em").text(function(d) {
			switch(d.id) {
			case "US" :
				return "Greece";
				break;
			case "LU" :
				return "Luxembourg";
				break;
			case "RU" :
				return "Russia";
				break;
			case "IT" :
				return "Italy";
				break;
			case "GR" :
				return "United States";
				break;
			case "FR" :
				return "France";
				break;
			case "DE" :
				return "Denmark";
				break;
			case "BR" :
				return "Brazil";
				break;
			case "BE" :
				return "Belgium";
				break;
				default : return d.id
				break;
			}
		}).style("font-weight", function (d) {
			switch (d.type) {
			case "CountryOfRisk" :
				return "bold";
				break;
			}
		}).style("font-size", function (d) {
			switch (d.type) {
			case "CountryOfRisk" :
				return "20px";
				break;
			}
		});
		
		

		// *******************************Build Dynamic Legend*****************************;
		legend = svg.append("g").attr("class", "legend").attr("transform",
				"translate(" + (width + 65) + ",40)")
				.style("font-size", "12px").call(getLegend);

		// *******************************End Dynamic Legend*****************************;

		// Start **********************Action to take on mouse double click**************************
		function dblclick() {

			d3.event.stopPropagation();
			
			if (d3.event.defaultPrevented)
				return; // ignore drag and zoom

			optArray.length = 0; // Clear autocomplete array
			// alert("ID IS " + d.id);
			angular.element(document.getElementById('controller')).scope()
					.$apply(
							function() {
								angular.element(
										document.getElementById('controller'))
										.scope().fetchOnClick(d.id, d.type);
							});
			
			document.getElementById('radiusType').selectedIndex = 0;
			
		}
		// End ******************************DoubleClick********************************************

		// prevent browser's default behavior for zoomIn zoomOut
		if (d3.event) {
			d3.event.preventDefault();
		}

		// **************************Highlight Connected nodes****************************

		// Toggle stores whether the highlighting is on
		var toggle = 0;

		// Create an array logging what is connected to what
		var linkedByIndex = {};
		for (i = 0; i < d3.values(nodes).length; i++) {
			linkedByIndex[i + "," + i] = 1;
		}
		;
		links.forEach(function(d) {
			linkedByIndex[d.source.index + "," + d.target.index] = 1;
		});

		// This function looks up whether a pair are neighbors
		function neighboring(a, b) {
			return linkedByIndex[a.index + "," + b.index];
		}

		function connectedNodes() {
			// d3.event.stopPropagation(); // Avoid zoom on doubleclick
			if (d3.event.defaultPrevented)
				return; // ignore calling this function on dragging the nodes.
			if (toggle == 0) {
				// Reduce the opacity of all but the neighboring nodes
				d = d3.select(this).node().__data__;
				node.style("opacity", function(o) {
					return neighboring(d, o) | neighboring(o, d) ? 1 : 0.05;
				});

				path.style("opacity", function(o) {
					return d.index == o.source.index
							| d.index == o.target.index ? 1 : 0.05;
				});

				// Reduce the op
				toggle = 1;
			} else {
				// Put them back to opacity=1
				node.style("opacity", 1);
				path.style("opacity", 1);
				toggle = 0;
			}

		}

		// ****************End Highlight Connected Nodes***********************

		// **************** Start Search Node Autocomplete***********************
		for (var i = 0; i < d3.values(nodes).length; i++) {
			if(d3.values(nodes)[i].name) {
			optArray.push(d3.values(nodes)[i].name);
			}
		}
		optArray = optArray.sort();
		
		$("#search").autocomplete({
			source : optArray
		});
		// **************** End Search Node Autocomplete***********************
		
	} // End json parsing
	
	//Imp to call here since searchNode is ouside this function. This ensures that the function is accessible.
	searchNode();
  //collide(0.5);
}

// Search a particular node in the graph by name (called from html onsubmit)
function searchNode() {
	// find the node
	var myNode;
	var txt = "";
	var selectedVal = document.getElementById('search').value;
	//alert("selectedVal: " + selectedVal);
	var node = svg.selectAll(".node");
	if (selectedVal != "") {
		// alert("else")
		var selected = node.filter(function(d, i) {
			if (d.name == selectedVal) {
				myNode = d;
			}
			return d.name != selectedVal;
		});
		selected.style("opacity", "0");
		//alert(myNode);
		if (myNode) { // Not working
			var dcx = (width / 2 - myNode.x * zoom.scale());
			var dcy = (height / 2 - myNode.y * zoom.scale());
			zoom.translate([ dcx, dcy ]);
			svg.attr("transform", "translate(" + dcx + "," + dcy + ")scale("
					+ zoom.scale() + ")");
			var link = svg.selectAll(".link")
			link.style("opacity", "0");
			d3.selectAll(".node, .link").transition().duration(5000).style(
					"opacity", 1);
		} else {
			txt = "Name not found!";
			document.getElementById("search").innerHTML = txt;
		}
	}
}

//Change the radius based on duration or amount(Called on change from html of "radiusType" dropdown)
function changeRadius() {

	radiusType = document.getElementById("radiusType").value;

	if (radiusType == "duration") {
		node.selectAll("circle").transition().duration(500).attr("r", function(d) {
			return scaleDuration(d.exposureDuration);
		});
	} else if (radiusType == "amount") {
		node.selectAll("circle").transition().duration(500).attr("r", function(d) {
			return scale(d.exposureAmount);
		});
	}
	
	force.on("tick", tick).start();

}

// Add connected lines between node
function tick() {

	radiusType = document.getElementById("radiusType").value;

	path
			.attr(
					"d",
					function(d) {
						if(d.source.name == d.target.name) {
							return;
						}
						// Total difference in x and y from source to target
						var dx = d.target.x - d.source.x, dy = d.target.y
								- d.source.y;
						// Length of path from center of source node to center
						// of target node
						dr = Math.sqrt((dx * dx) + (dy * dy));
						// alert(d.target);
						if (radiusType == "amount") {
							
							if (d.target.exposureAmount == null) {
								console.log("amount:if "+d.target.id +" & amount is "+d.target.exposureAmount);
								offsetX = (dx * 5) / dr;
								offsetY = (dy * 5) / dr;
							} else {
								console.log("amount:else "+d.target.id +" & amount is "+d.target.exposureAmount);
								
								//Offset needed inorder to subtract it from the actual length of the link
								//Ensures exact positioning of the arrow on the circumference. Else it gets hidden behind the circle
								offsetX = (dx * scale(d.target.exposureAmount))
										/ dr;
								offsetY = (dy * scale(d.target.exposureAmount))
										/ dr;
							}
						} else if (radiusType == "duration") {
							console.log("duration");
							if (d.target.exposureDuration == null) {
								offsetX = (dx * 5) / dr;
								offsetY = (dy * 5) / dr;
							} else {
								offsetX = (dx * scaleDuration(d.target.exposureDuration))
										/ dr;
								offsetY = (dy * scaleDuration(d.target.exposureDuration))
										/ dr;
							}
						}

						return "M" + d.source.x + "," + d.source.y
								+ "L" + (d.target.x - offsetX) + ","
								+ (d.target.y - offsetY);

					});

	node.attr("transform", function(d) {
		return "translate(" + d.x + "," + d.y + ")";
	});
	//node.each(collide(0.5));
}


//Avoid overlapping between nodes
function collide(alpha) {
	  return function(d) {
	    var rb = 2*radius + padding,
	        nx1 = d.x - rb,
	        nx2 = d.x + rb,
	        ny1 = d.y - rb,
	        ny2 = d.y + rb;
	    quadtree.visit(function(quad, x1, y1, x2, y2) {
	      if (quad.point && (quad.point !== d)) {
	        var x = d.x - quad.point.x,
	            y = d.y - quad.point.y,
	            l = Math.sqrt(x * x + y * y);
	          if (l < rb) {
	          l = (l - rb) / l * alpha;
	          d.x -= x *= l;
	          d.y -= y *= l;
	          quad.point.x += x;
	          quad.point.y += y;
	        }
	      }
	      return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
	    });
	  };
	}