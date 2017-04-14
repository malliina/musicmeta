var webSocket;

var tableContent;

var onconnect = function (payload) {
    setStatus("Connected.");
    webSocket.send(JSON.stringify({cmd: "subscribe"}));
};
var onmessage = function (payload) {
    var event = jQuery.parseJSON(payload.data);
    if (event.event == "ping") {

    } else {
        prepend(event);
    }
};
var onclose = function (payload) {
    setStatus("Closed.");
};
var onerror = function (payload) {
    setStatus("Error.");
};
// case class LogEvent(timeStamp: Long, timeFormatted:String, message: String, loggerName: String, threadName: String, level: Level)
var prepend = function (e) {
    var trc;
    if (e.level == "ERROR") {
        trc = "danger";
    } else {
        trc = "";
    }
    tableContent.prepend("<tr class=" + trc + "><td>" + e.timeFormatted + "</td><td>" + e.message + "</td><td>" + e.loggerName + "</td><td>" + e.level + "</td></tr>")
};
var setStatus = function (newStatus) {
    document.getElementById("status").innerHTML = newStatus;
};

var openSocket = function (wsUrl) {
    var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
    webSocket = new WS(wsUrl);
    webSocket.onopen = onconnect;
    webSocket.onmessage = function (evt) {
        onmessage(evt)
    };
    webSocket.onclose = onclose;
    webSocket.onerror = onerror;
};

$(document).ready(function () {
    tableContent = $("#log-table-body");
    // http://stackoverflow.com/a/6941653
    var scheme = location.protocol == "https:" ? "wss:" : "ws:";
    var full = scheme + '//' + location.hostname + (location.port ? ':' + location.port : '');
    openSocket(full + "/ws?f=json");
});
