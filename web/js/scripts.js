/**
 * Toggle the state of all checkboxes in a form based on the state of the master checkbox
 * Change the highlights in the rows
 *
 * @param (String) The id of the master checkbox
 * @param (String) The name of the array of checkboxes controlled by the master
 */
function jqCheckAll( id, name )
{
	$("INPUT[@name=" + name + "][type='checkbox']").attr('checked', $('#' + id).is(':checked'));
	if($('#' + id).is(':checked'))
	{
		$("tr.odd").css("background", "#cdf5ff");
		$("tr.even").css("background", "#cdf5ff");			

		setMessage("Selected All Records on This Page", "success");
	}
	else
	{
			$("tr.odd").css("background", "#ffffff");
			$("tr.even").css("background", "#ffffff");
	}
}

/**
 * Toggle highlight the background of a row in the table once its corresponding checkbox is checked
 *
 * @param (HTMLElement) The checkbox that was clicked
 */
function highlightRow(cbox)
{
	if(cbox.checked)
			cbox.parentNode.parentNode.setAttribute('style', 'background: #cdf5ff');
	else
			cbox.parentNode.parentNode.setAttribute('style', 'background: #FFFFFF');
}

/**
 * Function to display an informative message after user interaction
 *
 */
function setMessage(msgText, msgType)
{
	$("#message").text(msgText);
	$("#message").removeClass().addClass(msgType);
	$("#message").show('blind');
	setTimeout("$('#message').hide()", 5000);
}

/**
 * Function to display fancy tooltips next to the specified element(s)
 *
 */
function registerTooltips(formName)
{
	// select all desired input fields and attach tooltips to them
	$("#"+formName+" :input").tooltip({
		// place tooltip on the right edge
		position: "center right",
		// a little tweaking of the position
		offset: [-2, 25],
		// use the built-in fadeIn/fadeOut effect
		effect: "fade",
		// custom opacity setting
		opacity: 0.7
	});

	$(".edit-control").tooltip({
		// place tooltip on the right edge
		position: "center right",
		// a little tweaking of the position
		offset: [-2, 25],
		// use the built-in fadeIn/fadeOut effect
		effect: "fade",
		// custom opacity setting
		opacity: 0.7
	});
	
}

/**
 * Function to activate animations on all buttons on this page
 *
 */
function registerButtons()
{
	$('.operation')
	.css( {backgroundPosition: "0px 0px"} )
	.mouseover(function(){
		$(this).stop().animate(
			{backgroundPosition:"(-255px 0)"}, 
			{duration:700})
		})
	.mouseout(function(){
		$(this).stop().animate(
			{backgroundPosition:"(0px 0)"}, 
			{duration:700})
		});	
}


/**
 * @author Alexander Farkas
 * v. 1.02
 * Function for the button animations
 */
(function($) {
	$.extend($.fx.step,{
	    backgroundPosition: function(fx) {
            if (fx.state === 0 && typeof fx.end == 'string') {
                var start = $.curCSS(fx.elem,'backgroundPosition');
                start = toArray(start);
                fx.start = [start[0],start[2]];
                var end = toArray(fx.end);
                fx.end = [end[0],end[2]];
                fx.unit = [end[1],end[3]];
			}
            var nowPosX = [];
            nowPosX[0] = ((fx.end[0] - fx.start[0]) * fx.pos) + fx.start[0] + fx.unit[0];
            nowPosX[1] = ((fx.end[1] - fx.start[1]) * fx.pos) + fx.start[1] + fx.unit[1];
            fx.elem.style.backgroundPosition = nowPosX[0]+' '+nowPosX[1];

           function toArray(strg){
               strg = strg.replace(/left|top/g,'0px');
               strg = strg.replace(/right|bottom/g,'100%');
               strg = strg.replace(/([0-9\.]+)(\s|\)|$)/g,"$1px$2");
               var res = strg.match(/(-?[0-9\.]+)(px|\%|em|pt)\s(-?[0-9\.]+)(px|\%|em|pt)/);
               return [parseFloat(res[1],10),res[2],parseFloat(res[3],10),res[4]];
           }
        }
	});
})(jQuery);