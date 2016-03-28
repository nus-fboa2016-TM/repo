var COURSE_PANELS_TO_AUTO_LOAD_COUNT = 3;
var CURRENT_YEAR = (new Date()).getFullYear();

$(document).ready(function() {

    bindDeleteButtons();
    bindRemindButtons();
    bindPublishButtons();
    bindUnpublishButtons();
    
    setupFsCopyModal();
    
    //Click event binding for radio buttons
    var $radioButtons = $('label[name="sortby"]');
    $.each($radioButtons, function() {
        $(this).click(function () {
            var currentPath = window.location.pathname;
            var query = window.location.search.substring(1);
            var params = {};
            
            var paramValues = query.split('&');
            for (var i = 0; i < paramValues.length; i++) {
                var paramValue = paramValues[i].split('=');
                params[paramValue[0]] = paramValue[1];
            }

            if ('user' in params == false) {
                params['user'] = $('input[name="user"]').val();
            }

            window.location.href = currentPath + '?user=' + params['user'] + '&sortby=' + $(this).attr('data');
        });
    });
    
    // AJAX loading of course panels
    var $coursePanels = $('div[id|="course"]');
    $.each($coursePanels, function() {
        $(this).filter(function() {
            var isNotLoaded = $(this).find('form').length;
            return isNotLoaded;
        }).click(function() {
            var $panel = $(this);
            var formData = $panel.find('form').serialize();
            var content = $panel.find('.pull-right')[0];
            
            $.ajax({
                type : 'POST',
                url : '/page/instructorHomePage?' + formData,
                beforeSend : function() {
                    $(content).html("<img src='/images/ajax-loader.gif'/>");
                },
                error : function() {
                    var warningSign = '<span class="glyphicon glyphicon-warning-sign"></span>';
                    var errorMsg = '[ Failed to load. Click here to retry. ]';
                    errorMsg = '<strong style="margin-left: 1em; margin-right: 1em;">' + errorMsg + '</strong>';
                    var chevronDown = '<span class="glyphicon glyphicon-chevron-down"></span>';
                    $(content).html(warningSign + errorMsg + chevronDown);  
                },
                success : function(data) {
                    // .outerHTML is used instead of jQuery's .replaceWith() to avoid the <span>
                	// for statuses' tooltips from being closed due to the presence of <br>
                    $panel[0].outerHTML = data;
                    linkAjaxForResponseRate();
                }
            });
        });
    });
    
    // Automatically load top few course panels
    $coursePanels.slice(0, COURSE_PANELS_TO_AUTO_LOAD_COUNT).click();
});

/**
 * This is the function invoked when an instructor clicks on the archive button, which asks the instructor
 * to confirm whether or not the course should be archived
 * 
 * @param courseId
 * @returns a boolean to either continue or stop the action from continuing
 */
function toggleArchiveCourseConfirmation(courseId) {
    return confirm('Are you sure you want to archive ' + courseId + '? This action can be reverted'
                   + ' by going to the "courses" tab and unarchiving the desired course(s).');
}

/**
 * This is the comparator that is used for sorting start and end times on the InstructorHome page
 * It works by adding on the current year to time values and then comparing them.
 * This is needed since the time in the table on the InstructorHome page doesn't include the year component
 *
 * @param x
 * @param y
 * @returns 1 if Date x is after y, 0 if same and -1 if before
 */
function instructorHomeDateComparator(x, y) {
    x = Date.parse(x + ' ' + CURRENT_YEAR);
    y = Date.parse(y + ' ' + CURRENT_YEAR);
    var comparisonResult = (x > y) ? 1 : (x < y) ? -1 : 0;
    return comparisonResult;
}
