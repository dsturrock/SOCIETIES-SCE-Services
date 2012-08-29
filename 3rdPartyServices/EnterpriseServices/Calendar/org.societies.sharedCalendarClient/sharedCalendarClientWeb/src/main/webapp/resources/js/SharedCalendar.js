

var year = new Date().getFullYear();
var month = new Date().getMonth();
var day = new Date().getDate();

var eventData = {
		events : [
		{"id":1, "start": new Date(year, month, day, 12), "end": new Date(year, month, day, 13, 35),"title":"Lunch with Mike"},
		{"id":2, "start": new Date(year, month, day, 14), "end": new Date(year, month, day, 14, 45),"title":"Dev Meeting"}
		]
		};
//JQuery Stuff
jQuery.noConflict();	
jQuery(document).ready(function($) {
	var ua = navigator.userAgent.toLowerCase();
	var isAndroid = ua.indexOf("android") > -1; //&& ua.indexOf("mobile");
	if(isAndroid) {
		$('#calendar').weekCalendar({
			timeslotsPerHour: 2,
			height: function($calendar){
				return $(window).height() - $('#divHeader').outerHeight();
			},
			eventRender : function(calEvent, $event) {
				if(calEvent.end.getTime() < new Date().getTime()) {
					$event.css("backgroundColor", "#aaa");
					$event.find(".time").css({"backgroundColor": "#999", "border":"1px solid #888"});
				}
			},
			eventNew : function(calEvent, $event) {
				displayMessage("<strong>Added event</strong><br/>Start: " + calEvent.start + "<br/>End: " + calEvent.end);
				alert("You've added a new event. You would capture this event, add the logic for creating a new event with your own fields, data and whatever backend persistence you require.");
			},
			eventDrop : function(calEvent, $event) {
				displayMessage("<strong>Moved Event</strong><br/>Start: " + calEvent.start + "<br/>End: " + calEvent.end);
			},
			eventResize : function(calEvent, $event) {
				displayMessage("<strong>Resized Event</strong><br/>Start: " + calEvent.start + "<br/>End: " + calEvent.end);
			},
			eventClick : function(calEvent, $event) {
				displayMessage("<strong>Clicked Event</strong><br/>Start: " + calEvent.start + "<br/>End: " + calEvent.end);
			},
			eventMouseover : function(calEvent, $event) {
				displayMessage("<strong>Mouseover Event</strong><br/>Start: " + calEvent.start + "<br/>End: " + calEvent.end);
			},
			eventMouseout : function(calEvent, $event) {
				displayMessage("<strong>Mouseout Event</strong><br/>Start: " + calEvent.start + "<br/>End: " + calEvent.end);
			},
			noEvents : function() {
				displayMessage("There are no events for this week");
			},
			data: eventData
		
		});
	}else{


	$('#calendar').weekCalendar({
		timeslotsPerHour: 4,
		height: function($calendar){
			return $(window).height() - $('#divHeader').outerHeight();
		},
		eventRender : function(calEvent, $event) {
			if(calEvent.end.getTime() < new Date().getTime()) {
				$event.css("backgroundColor", "#aaa");
				$event.find(".time").css({"backgroundColor": "#999", "border":"1px solid #888"});
			}
		},
		eventNew : function(calEvent, $event) {
			displayMessage("<strong>Added event</strong><br/>Start: " + calEvent.start + "<br/>End: " + calEvent.end);
			alert("You've added a new event. You would capture this event, add the logic for creating a new event with your own fields, data and whatever backend persistence you require.");
		},
		eventDrop : function(calEvent, $event) {
			displayMessage("<strong>Moved Event</strong><br/>Start: " + calEvent.start + "<br/>End: " + calEvent.end);
		},
		eventResize : function(calEvent, $event) {
			displayMessage("<strong>Resized Event</strong><br/>Start: " + calEvent.start + "<br/>End: " + calEvent.end);
		},
		eventClick : function(calEvent, $event) {
			displayMessage("<strong>Clicked Event</strong><br/>Start: " + calEvent.start + "<br/>End: " + calEvent.end);
		},
		eventMouseover : function(calEvent, $event) {
			displayMessage("<strong>Mouseover Event</strong><br/>Start: " + calEvent.start + "<br/>End: " + calEvent.end);
		},
		eventMouseout : function(calEvent, $event) {
			displayMessage("<strong>Mouseout Event</strong><br/>Start: " + calEvent.start + "<br/>End: " + calEvent.end);
		},
		noEvents : function() {
			displayMessage("There are no events for this week");
		},
		data: eventData
	
	});
	}

	function displayMessage(message) {
		$("#message").html(message).fadeIn();
	}

	$("<div id=\"message\" class=\"ui-corner-all\"></div>").prependTo($("body"));
	
});    	


//Prototype.js handlers, 
//defined AFTER the document has loaded

document.observe("dom:loaded", function() {
	  
	
$("createCisCalendarButton").on("click", function(event){
new Ajax.Updater('result','/sharedCal/createCisCalendarAjax.do',
		  {
			parameters: { cisId: $F('cisId'), cisSummary: $F('cisSummary') },
		    method:'get',
		    onSuccess: function(transport){
		      var response = transport.responseText || "no response text";
		      $('result').replace("<p>CIS Calendar created successfully.</p>");
		    },
		    onFailure: function(){ alert('Something went wrong...'); }
		  });			
});

$("getCisCalendarEventsButton").on("click", function(event){
	new Ajax.Updater('result','/sharedCal/getCisCalendarEvents.do',
			  {
				parameters: { calendarId: $F('calId')},
			    method:'get',
			    onSuccess: function(transport){
			      var response = transport.responseText || "no response text";
			      var ajResp = eval("(" + response + ")"); 
			      eventData.events = ajResp;
			      jQuery("#calendar").show();
			      jQuery("#calendar").weekCalendar('refresh');
			      $('eventsTableBody').update("");
			      $('eventsTableBody').insert({
			    	  bottom: new Element('tr').update("<th>Event Id</th><th>Event Start</th><th>Event End</th><th>Title</th>")
			      });
			      for (var index = 0, len = ajResp.length; index < len; ++index) {
			    	  var item = ajResp[index];
			    	  $('eventsTableBody').insert({
				    	  bottom: new Element('tr', {id:item.id}).update("<td>"+item.id+"</td><td>"+item.start+"</td><td>"+item.end+"</td><td>"+item.title+"</td>")
				      });
			    	  Event.observe(item.id, 'click', function(event) {
			    		    $('cisSummary').value = this.id;
			    	  });
			      }			      
			    },
			    onFailure: function(){ alert('Something went wrong...'); }
			  });			
	});

$("deleteCisCalendarButton").on("click", function(event){
	new Ajax.Updater('result','/sharedCal/deleteCisCalendar.do',
			  {
				parameters: { calendarId: $F('calId')},
			    method:'get',
			    onSuccess: function(transport){
			      var response = transport.responseText || "no response text";
			      $('result').replace("<p>CIS Calendar deleted successfully.</p>");
			    },
			    onFailure: function(){ alert('Something went wrong...'); }
			  });			
	});

$("createCssCalendarButton").on("click", function(event){
	new Ajax.Updater('result','/sharedCal/createCssCalendarAjax.do',
			  {
				parameters: { cssSummary: $F('cisSummary') },
			    method:'get',
			    onSuccess: function(transport){
			      var response = transport.responseText || "no response text";
			      $('result').replace("<p>CSS Calendar created successfully.</p>");
			    },
			    onFailure: function(){ alert('Something went wrong...'); }
			  });			
	});

$("deleteCssCalendarButton").on("click", function(event){
	new Ajax.Updater('result','/sharedCal/deletePrivateCalendar.do',
		  {
			method:'get',
		    onSuccess: function(transport){
		      var response = transport.responseText || "no response text";
		      $('result').replace("<p>CSS Calendar deleted successfully.</p>");
		    },
		    onFailure: function(){ alert('Something went wrong...'); }
		  });			
});

$("retrieveCssCalendarEvents").on("click", function(event){
new Ajax.Updater('result','/sharedCal/getPrivateEvents.do',
		  {
			method:'get',
		    onSuccess: function(transport){
		      var response = transport.responseText || "no response text";
		      var ajResp = eval("(" + response + ")"); 
		      //alert("Success! \n\n" + response);
		      eventData.events = ajResp;
		      jQuery("#calendar").show();
		      jQuery("#calendar").weekCalendar('refresh'); 
		     
		    },
		    onFailure: function(){ alert('Something went wrong...'); }
		  });			
});

$("getAllRelevantCIS").on("click", function(event){
	new Ajax.Updater('result','/sharedCal/getAllRelevantCis.do',
		  {
			method:'get',
		    onSuccess: function(transport){
		      var response = transport.responseText || "no response text";
		      //alert("Success! \n\n" + response);
		      var ajResp = eval("(" + response + ")");
		      $('cisTableBody').update("");
		      $('cisTableBody').insert({
		    	  bottom: new Element('tr').update("<th>Cis ID</th><th>Cis Name</th>")
		      });
		      for (var index = 0, len = ajResp.length; index < len; ++index) {
		    	  var item = ajResp[index];
		    	  $('cisTableBody').insert({
			    	  bottom: new Element('tr', {id:item.id}).update("<td>"+item.id+"</td><td>"+item.name+"</td>")
			      });
		    	  Event.observe(item.id, 'click', function(event) {
		    		    $('cisId').value = this.id;
		    	  });
		      }
		    },
		    onFailure: function(){ alert('Something went wrong...'); }
		  }
	);			
});


$("getAllCisCalendarsAjax").on("click", function(event){
	new Ajax.Updater('result','/sharedCal/getAllCisCalendarsAjax.do',
		  {
			parameters: { cisId: $F('cisId')},
		    method:'get',
		    onSuccess: function(transport){
		      var response = transport.responseText || "no response text";
		      //alert("Success! \n\n" + response);
		      var ajResp = eval("(" + response + ")");
		      var calList = ajResp.calendarList; 
		      $('calendarsTableBody').update("");
		      $('calendarsTableBody').insert({
		    	  bottom: new Element('tr').update("<th>Calendar ID</th><th>Calendar Summary</th>")
		      });
		      for (var index = 0, len = calList.length; index < len; ++index) {
		    	  var item = calList[index];
		    	  $('calendarsTableBody').insert({
			    	  bottom: new Element('tr', {id:item.calendarId}).update("<td>"+item.calendarId+"</td><td>"+item.summary+"</td>")
			      });
		    	  Event.observe($(item.calendarId), 'click', function(event) {
		    		    $('calId').value = this.id;
		    	  });
		      }
		    },
		    onFailure: function(){ alert('Something went wrong...'); }
		  }
	);			
});

//First time initializations
jQuery("#calendar").hide();

});