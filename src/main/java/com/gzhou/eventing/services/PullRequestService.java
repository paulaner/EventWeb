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

import java.util.ArrayList;
import java.util.List;

@Service
public class PullRequestService {

    private static final Logger logger = Logger.getLogger(PullRequestService.class);

    @Autowired
    private JsonToTextUtil jsonToTextUtil;

    @Autowired
    private RestHelper restHelper;

    public void postPullRequestEventToSlack(Object payload) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        String payloadString = mapper.writeValueAsString(payload);
        JSONObject obj = new JSONObject(payloadString);

        JSONObject pullRequestObj = obj.getJSONObject("pull_request");

        int pullRequestNum = pullRequestObj.getInt("number");
        String pullRequestUrl = pullRequestObj.getString("html_url");
        String pullRequestName = "#" + pullRequestNum + " " + pullRequestObj.getString("title");
        String pullRequestBodyText = pullRequestObj.getString("body");

        String userName = obj.getJSONObject("sender").getString("login");
        String userUrl = obj.getJSONObject("sender").getString("html_url");
        String userNameAndLink = "<" + userUrl + "|@" + userName + ">";

        JSONObject repoObj = obj.getJSONObject("repository");
        String repoFullName = repoObj.getString("full_name");
        String repoUrl = repoObj.getString("html_url");
        String repoNameAndLink = "[<" + repoUrl + "|" + repoFullName + ">]";

        // process different pull request event types
        String actionType = obj.getString("action");
        JSONObject json = null;
        switch (actionType) {
            case "opened":
                String newOrEdit = " Pull request submitted by ";
                String pullRequestText = repoNameAndLink + newOrEdit + userNameAndLink;
                json = jsonToTextUtil.toJSONTitleAndOneLine("GitHub", pullRequestText, pullRequestName,
                        pullRequestUrl, pullRequestBodyText, "#33cc33");
                break;
            case "edited":
                newOrEdit = " Pull request is edited by ";
                pullRequestText = repoNameAndLink + newOrEdit + userNameAndLink;
                json = jsonToTextUtil.toJSONTitleAndOneLine("GitHub", pullRequestText, pullRequestName,
                        pullRequestUrl, pullRequestBodyText, "#ff4d4d");
                break;
            case "synchronize":
                JSONObject headObj = pullRequestObj.getJSONObject("head");
                String headCommitHash = headObj.getString("sha").length() > 8 ?
                        headObj.getString("sha").substring(0, 8) : headObj.getString("sha");
                String text = repoNameAndLink + " 1 new commit(s) by " + userNameAndLink + " on Pull Request ";
                List<String> commitList = new ArrayList<>();
                String commitAndLink = "<" + pullRequestUrl + "|" + headCommitHash + ">  -" + pullRequestBodyText;
                commitList.add(commitAndLink);
                json = jsonToTextUtil.toJSONPushCommit("GitHub", text, commitList);
                break;
            case "closed":
                String pullRequestNameAndLink = "<" + pullRequestUrl + "|" + pullRequestName + ">";
                String pullRequestVerb = pullRequestObj.getBoolean("merged") ?
                        " merged and closed " : " closed ";
                text = "Pull request " + pullRequestNameAndLink + pullRequestVerb + " by " + userNameAndLink;
                json = jsonToTextUtil.toJSONIssue("GitHub", null, null, null, text, "#bfbfbf");
                break;
            case "reopened":
                newOrEdit = " Reopened Pull request: ";
                pullRequestText = userNameAndLink + newOrEdit + repoNameAndLink;
                json = jsonToTextUtil.toJSONTitleAndOneLine("GitHub", pullRequestText, pullRequestName,
                        pullRequestUrl, pullRequestBodyText, "#33cc33");
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


}
