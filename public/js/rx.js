var webSocket;

var tableContent;

var onconnect = function (payload) {
    setStatus("Connected.");
    webSocket.send(JSON.stringify({cmd: "subscribe"}));
};
var onmessage = function (payload) {
    var data = jQuery.parseJSON(payload.data);
    if (data.event == "ping") {

    } else {
        prepend(data.events);
    }
};
var onclose = function (payload) {
    setStatus("Closed.");
};
var onerror = function (payload) {
    setStatus("Error.");
};
// case class LogEvent(timeStamp: Long, timeFormatted:String, message: String, loggerName: String, threadName: String, level: Level)
var prepend = function (events) {
    events.forEach(prependSingle);
};
var prependSingle = function (event) {
    var trc;
    if (event.level == "ERROR") {
        trc = "danger";
    } else {
        trc = "";
    }
    tableContent.prepend("<tr class=" + trc + "><td>" + event.timeFormatted + "</td><td>" + event.message + "</td><td>" + event.loggerName + "</td><td>" + event.level + "</td></tr>")

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
