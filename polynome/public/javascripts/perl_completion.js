var done = 0;
var simulation_output = '';
var htmlBody = document.getElementById("bodytag");

function check_perl_completed(prefix) {
	if(done==0) {
		var node = document.createElement("script");
		node.src = "/perl/" + prefix + ".done.js?t=" + (new Date).getTime();
		htmlBody.appendChild(node);
		setTimeout("check_perl_completed('" + prefix + "')", 5000);
	} else if (done==1) {
		var node = document.getElementById("completion_msg");
		node.innerHTML = '<br>' + simulation_output + '<br><strong>Your data has been generated successfully!</strong>';
	} else {
		var node = document.getElementById("completion_msg");
		node.innerHTML = '<br>' + simulation_output + '<br><font color=red><strong>Your data has not been generated successfully!</strong></font>';
    }
}
