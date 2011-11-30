this.screenshotPreview = function() {
	xOffset = -10;
	yOffset = -50;
	$("a.screenshot").hover(
			function(e) {
				this.t = this.title;
				this.title = "";
				var c = (this.t != "") ? "<br/>" + this.t : "";
				$("body").append(
						"<p id='screenshot'><img src='" + this.rel + "' alt='url preview' width='200px'/>"
								+ c + "</p>");
				$("#screenshot").css("top", (e.pageY - xOffset) + "px").css("left",
						(e.pageX + yOffset) + "px").fadeIn("fast");
			}, function() {
				this.title = this.t;
				$("#screenshot").remove();
			});
	$("a.screenshot").mousemove(function(e) {
		$("#screenshot").css("top", (e.pageY - xOffset) + "px").css("left", (e.pageX + yOffset) + "px");
	});
};
$(document).ready(function() {
	screenshotPreview();
});
function newStateImagesWindow(viewNodeID) {
	var viewNode = $('#viewNode' + viewNodeID);
	var character = viewNode.find('span.character').html();
	var newPage = '<html><head><style type="text/css">body{ color:#111; font-family: Verdana, helvetica, arial, sans-serif; font-size: 78%;   background: #fff;}table { border-collapse:collapse; width:90%;}th, td { border:1px solid #ddd; width:20%;}td { text-align:center;}caption { font-weight:bold}</style></head><body><h2>'
			+ character + '</h2>';
	newPage += '<table cellpadding="5"><tr><th>state</th><th>image</th></tr>';
	for ( var i = 0; i < viewNode.find('span.state').size(); i++) {
		var state = viewNode.find('span.state')[i];
		var stateID = state.id.split('_')[1];
		var stateContent = state.innerHTML;
		var splitArray = stateContent.split(';');
		var stateImageURL = $('#stateImageURL_' + stateID);
		var stateImageURLContent = stateImageURL.html();
		stateContent = splitArray[splitArray.length - 1];
		newPage += '<tr>';
		var imgTag = '<center>No image</center>';
		if (stateImageURLContent.length > 0 && stateImageURLContent.indexOf('http://') == 0) {
			imgTag = '<img src="' + stateImageURLContent + '" width="200px" />';
		}
		newPage += '<td>' + stateContent + '</td><td>' + imgTag + '</td>';
		newPage += '</tr>';
	}
	newPage += '</table>';
	newPage += '</body></html>';
	var j = window.open('', 'State Illustrations', 'toolbar=0, width=800px, height=600px');
	j.document.write(newPage);
	j.document.close();
}

function newStateImagesWindowTree(characterName,characterStates,statesURLs){
	var newPage = '<html><head><style type="text/css">body{ color:#111; font-family: Verdana, helvetica, arial, sans-serif; font-size: 78%;   background: #fff;}table { border-collapse:collapse; width:90%;}th, td { border:1px solid #ddd; width:20%;}td { text-align:center;}caption { font-weight:bold}</style></head><body><h2>'
		+ characterName + '</h2>';
	newPage += '<table cellpadding="5"><tr><th>state</th><th>image</th></tr>';
	
	for(var i = 0 ; i < characterStates.length ; i++ ){
		var state = characterStates[i];
		var stateImageURL = statesURLs[i];
		newPage += '<tr>';
		var imgTag = '<center>No image</center>';
		if(stateImageURL.length > 0 && stateImageURL.indexOf('http://') == 0){
			imgTag = '<img src="' + stateImageURL + '" width="200px" />';
		}
		newPage += '<td>' + state + '</td><td>' + imgTag + '</td>';
		newPage += '</tr>';
	}
	newPage += '</table>';
	newPage += '</body></html>';
	var j = window.open('', 'State Illustrations', 'toolbar=0, width=800px, height=600px');
	j.document.write(newPage);
	j.document.close();
}

function newSingleStateImageWindow(imageURL) {
	var newPage = '<html><head></head><body><img src="'+imageURL+'"/></body></html>';
	var j = window.open('', 'State Illustration', 'toolbar=0');
	j.document.write(newPage);
	j.document.close();
}

function goToViewNode(viewNodeID) {
	viewNodeHistory.push(viewNodeID);
	toggleViewNode(viewNodeID);
	displayViewNodeStateImages(viewNodeID);
}

function goToPreviousViewNode() {
	if (viewNodeHistory.length <= 1) {
		toggleViewNode(1);
	} else {
		viewNodeHistory.pop();
		var previousViewNodeID = viewNodeHistory.pop();
		goToViewNode(previousViewNodeID);
	}
}

function goToFirstViewNode() {
	viewNodeHistory = [];
	goToViewNode(1);
	displayViewNodeStateImages(1);
}

function toggleViewNode(viewNodeID) {
	$('.viewNode').hide();
	$('#viewNode' + viewNodeID).show();
	return false;
}

function displayViewNodeStateImages(viewNodeID) {
	for ( var i = 0; i < $('#viewNode' + viewNodeID).children(".stateImageURLandContainer").size(); i++) {
		var siuc = $($('#viewNode' + viewNodeID).children(".stateImageURLandContainer")[i]);
		var imageContainer = siuc.find(".stateImageContainer");
		var imageURL = jQuery.trim(siuc.find('.stateImageURL').text());

		if (imageURL != null && imageURL.length > 0 && siuc.find(".stateImageInLine").size() == 0) {
			imageContainer.append('<img onClick=\"newSingleStateImageWindow(\'' + imageURL
					+ '\')\" class="stateImageInLine" src="' + imageURL + '" />')
		}
	}
}

function initViewNodes() {
	$('#keyWait').remove();
	$('#keyBody').css('visibility', 'visible');
	goToFirstViewNode();

}

function initTree() {
	$('#tree').treeview({
		collapsed : true, unique : false, control : "#treecontrol", persist : 'location'
	});
}

var viewNodeHistory = [];
