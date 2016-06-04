package com.gzhou.eventing.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gzhou.eventing.util.JsonToTextUtil;
import com.gzhou.eventing.util.RestHelper;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class IssueService {

    private static final Logger logger = Logger.getLogger(IssueService.class);

    @Autowired
    private JsonToTextUtil jsonToTextUtil;

    @Autowired
    private RestHelper restHelper;

    /**
     * Process issue related event
     * Note: issue comments events will be processed by postIssueCommentEventToSlack(Object payload)
     * @param payload
     * @throws Exception
     */
    public void postIssueEventToSlack(Object payload) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        String payloadString = mapper.writeValueAsString(payload);
        JSONObject obj = new JSONObject(payloadString);

        // parse payload
        String issueActionType = obj.getString("action");

        JSONObject issueObj = obj.getJSONObject("issue");
        String issueUrl = issueObj.getString("html_url");
        int issueNum = issueObj.getInt("number");
        String issueTitle = issueObj.getString("title");
        String issueFullName = "#" + issueNum + " " + issueTitle;
        String issueFirstComment = issueObj.getString("body");

        JSONObject userObj = issueObj.getJSONObject("user");
        String userName = userObj.getString("login");
        String userUrl = userObj.getString("html_url");

        JSONObject repoObj = obj.getJSONObject("repository");
        String repoFullName = repoObj.getString("full_name");
        String repoUrl = repoObj.getString("html_url");

        String repoNameAndLink = "<" + repoUrl + "|" + repoFullName + ">";
        String userNameAndLink = "<" + userUrl + "|@" + userName + ">";

        String textAndLink = repoNameAndLink + " Issue created by " + userNameAndLink;

        // process different types of issue events
        JSONObject json = null;
        switch (issueActionType) {
            case "opened":
                json = jsonToTextUtil.toJSONIssue("GitHub", textAndLink, issueFullName, issueUrl, issueFirstComment, "#ff8000");
                break;
            case "closed":
                String issueNameAndLink = "<" + issueUrl + "|" + issueFullName + ">";
                String text = repoNameAndLink + " Issue closed: " + issueNameAndLink + " by " + userNameAndLink;
                json = jsonToTextUtil.toJSONIssue("GitHub", null, null, null, text, "#bfbfbf");
                break;
            case "assigned":
                issueNameAndLink = "<" + issueUrl + "|" + issueFullName + ">";
                text = repoNameAndLink + " Issue assigned : " + issueNameAndLink + " to " + userNameAndLink;
                json = jsonToTextUtil.toJSONIssue("GitHub", null, null, null, text, "#1a75ff");
                break;
            case "unassigned":
                issueNameAndLink = "<" + issueUrl + "|" + issueFullName + ">";
                text = repoNameAndLink + " Issue : " + issueNameAndLink + " assignment removed from " + userNameAndLink;
                json = jsonToTextUtil.toJSONIssue("GitHub", null, null, null, text, "#bfbfbf");
                break;
            case "labeled":
                issueNameAndLink = "<" + issueUrl + "|" + issueFullName + ">";
                text = repoNameAndLink + " Issue : " + issueNameAndLink + " label added by " + userNameAndLink;
                json = jsonToTextUtil.toJSONIssue("GitHub", null, null, null, text, "#9999ff");
                break;
            case "unlabeled":
                issueNameAndLink = "<" + issueUrl + "|" + issueFullName + ">";
                text = repoNameAndLink + " " + userNameAndLink + " removed label from " + " Issue : " + issueNameAndLink;
                json = jsonToTextUtil.toJSONIssue("GitHub", null, null, null, text, "#9999ff");
                break;
            case "reopened":
                textAndLink = repoNameAndLink + " Issue reopened by " + userNameAndLink;
                json = jsonToTextUtil.toJSONIssue("GitHub", textAndLink, issueFullName, issueUrl, issueFirstComment, "#ff8000");
                break;
            case "edited":
                textAndLink = repoNameAndLink + " Issue edited by " + userNameAndLink;
                json = jsonToTextUtil.toJSONIssue("GitHub", textAndLink, issueFullName, issueUrl, issueFirstComment, "#36a64f");
                break;
            default:
                json = new JSONObject();
                break;
        }

        ResponseEntity<String> response = restHelper.sendRestCall(json);

        if (response.getStatusCode() == HttpStatus.OK) {
            logger.info(json.toString());
        }
    }

    /**
     * Only process issue comment related events
     * @param payload
     * @throws Exception
     */
    public void postIssueCommentEventToSlack(Object payload) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        String payloadString = mapper.writeValueAsString(payload);
        JSONObject obj = new JSONObject(payloadString);

        // parse git event payload
        String commentActionType = obj.getString("action");

        JSONObject issueObj = obj.getJSONObject("issue");
        String issueUrl = issueObj.getString("html_url");
        int issueNum = issueObj.getInt("number");

        JSONObject commentObj = obj.getJSONObject("comment");
        String commentUrl = commentObj.getString("html_url");
        String commentBody = commentObj.getString("body");

        JSONObject userObj = commentObj.getJSONObject("user");
        String userName = userObj.getString("login");
        String userUrl = userObj.getString("html_url");

        String userNameAndLink = "<" + userUrl + "|@" + userName + ">";

        // decide issue comment type
        Map<String, String> actionMap = new HashMap<>();
        actionMap.put("created", " added ");
        actionMap.put("edited", " edited ");
        actionMap.put("deleted", " deleted ");
        String action = actionMap.get(commentActionType);

        String commentPart = commentActionType.endsWith("created") ? "|a new comment>" : "|comment>";
        String issueOrPullRequest =" to issue ";
        String newCommentLink = action + "<" + commentUrl + commentPart + issueOrPullRequest;
        String issueCommentAndLink = "<" + issueUrl + "|" + "#ISSUE " + issueNum + ">";

        String textAndLink = userNameAndLink + newCommentLink + issueCommentAndLink;

        JSONObject json = jsonToTextUtil.toJSONIssueComment("GitHub", textAndLink, commentBody, "#ff8000");

        ResponseEntity<String> response = restHelper.sendRestCall(json);

        if (response.getStatusCode() == HttpStatus.OK) {
            logger.info(json.toString());
        }
    }


}
