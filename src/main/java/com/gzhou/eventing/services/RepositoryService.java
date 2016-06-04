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


@Service
public class RepositoryService {

    private static final Logger logger = Logger.getLogger(RepositoryService.class);

    @Autowired
    private JsonToTextUtil jsonToTextUtil;

    @Autowired
    private RestHelper restHelper;

    public void postRepoEventToSlack(Object payload) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        String payloadString = mapper.writeValueAsString(payload);
        JSONObject obj = new JSONObject(payloadString);

        JSONObject repoObj = obj.getJSONObject("repository");
        String repoFullName = repoObj.getString("full_name");
        String repoUrl = repoObj.getString("html_url");
        String repoNameAndLink = "[<" + repoUrl + "|" + repoFullName + ">]";

        String userName = obj.getJSONObject("sender").getString("login");
        String userUrl = obj.getJSONObject("sender").getString("html_url");
        String userNameAndLink = "<" + userUrl + "|@" + userName + ">";

        String actionType = obj.getString("action");

        JSONObject json = null;
        switch (actionType) {
            case "created":
                String repoText = userNameAndLink + " created new Repository";
                json = jsonToTextUtil.toJSONIssueComment("GitHub", repoText, repoNameAndLink, "#ffff66");
                break;
            case "deleted":
                repoText = userNameAndLink + " deleted Repository";
                json = jsonToTextUtil.toJSONIssueComment("GitHub", repoText, repoNameAndLink, "#ffff66");
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
