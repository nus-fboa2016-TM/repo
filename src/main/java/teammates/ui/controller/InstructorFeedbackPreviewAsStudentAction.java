package teammates.ui.controller;

import teammates.common.datatransfer.StudentAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.util.Assumption;
import teammates.common.util.Const;
import teammates.logic.api.GateKeeper;

public class InstructorFeedbackPreviewAsStudentAction extends Action {

    @Override
    protected ActionResult execute() throws EntityDoesNotExistException {
        String courseId = getRequestParamValue(Const.ParamsNames.COURSE_ID);
        String feedbackSessionName = getRequestParamValue(Const.ParamsNames.FEEDBACK_SESSION_NAME);
        String previewStudentEmail = getRequestParamValue(Const.ParamsNames.PREVIEWAS);

        Assumption.assertNotNull(String.format(Const.StatusMessages.NULL_POST_PARAMETER_MESSAGE, 
                                               Const.ParamsNames.COURSE_ID), 
                                               courseId);
        Assumption.assertNotNull(String.format(Const.StatusMessages.NULL_POST_PARAMETER_MESSAGE, 
                                               Const.ParamsNames.FEEDBACK_SESSION_NAME),
                                               feedbackSessionName);
        Assumption.assertNotNull(String.format(Const.StatusMessages.NULL_POST_PARAMETER_MESSAGE, 
                                               Const.ParamsNames.PREVIEWAS),
                                               previewStudentEmail);

        new GateKeeper().verifyAccessible(
                logic.getInstructorForGoogleId(courseId, account.googleId), 
                logic.getFeedbackSession(feedbackSessionName, courseId),
                false, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_SESSION);
        
        StudentAttributes previewStudent = logic.getStudentForEmail(courseId, previewStudentEmail);
        
        if (previewStudent == null) {
            throw new EntityDoesNotExistException("Student Email " +
                      previewStudentEmail + " does not exist in " + courseId +
                      ".");
        }
        
        FeedbackSubmissionEditPageData data = new FeedbackSubmissionEditPageData(account, student);
        
        data.bundle = logic.getFeedbackSessionQuestionsBundleForStudent(
                feedbackSessionName, courseId, previewStudent.email);
        
        // the following condition is not tested as typically the GateKeeper above handles
        // the case and it wont happen
        if (data.bundle == null) {
            throw new EntityDoesNotExistException("Feedback session " +
                      feedbackSessionName + " does not exist in " + courseId +
                      ".");
        }
        
        data.setSessionOpenForSubmission(true);
        data.setPreview(true);
        data.setHeaderHidden(true);
        data.setStudentToViewPageAs(previewStudent);
        data.setSubmitAction(Const.ActionURIs.STUDENT_FEEDBACK_SUBMISSION_EDIT_SAVE);
        data.bundle.resetAllResponses();

        statusToAdmin = "Preview feedback session as student (" + previewStudent.email + ")<br>" +
                        "Session Name: " + feedbackSessionName + "<br>" +
                        "Course ID: " + courseId;
        
        data.init("", "", courseId);
        
        return createShowPageResult(Const.ViewURIs.STUDENT_FEEDBACK_SUBMISSION_EDIT, data);
    }
}
