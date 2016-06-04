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
public class ForkService {

    private static final Logger logger = Logger.getLogger(ForkService.class);

    @Autowired
    private JsonToTextUtil jsonToTextUtil;

    @Autowired
    private RestHelper restHelper;

    public void postForkEventToSlack(Object payload) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        String payloadString = mapper.writeValueAsString(payload);
        JSONObject obj = new JSONObject(payloadString);

        JSONObject forkObj = obj.getJSONObject("forkee");
        String forkeeFullName = forkObj.getString("full_name");
        String forkeeUrl = forkObj.getJSONObject("owner").getString("html_url");
        String forkeeNameAndLink = "<" + forkeeUrl + "|" + forkeeFullName + ">";

        JSONObject sourceRepoObj = obj.getJSONObject("repository");
        String sourceRepoName = sourceRepoObj.getString("full_name");
        String sourceRepoUrl = sourceRepoObj.getJSONObject("owner").getString("html_url");
        String soureRepoNameAndLink = "<" + sourceRepoUrl + "|" + sourceRepoName + ">";

        String text = forkeeNameAndLink + " Forked " + soureRepoNameAndLink;
        JSONObject json = jsonToTextUtil.toJSONIssue("GitHub", null, null, null, text, "#66ccff");

        ResponseEntity<String> response = restHelper.sendRestCall(json);

        if (response.getStatusCode() == HttpStatus.OK) {
            logger.info(json.toString());
        }
    }
}
