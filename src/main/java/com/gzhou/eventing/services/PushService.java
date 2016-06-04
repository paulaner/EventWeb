package com.gzhou.eventing.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gzhou.eventing.util.Constants;
import com.gzhou.eventing.util.JsonToTextUtil;
import com.gzhou.eventing.util.RestHelper;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PushService {

    private static final Logger logger = Logger.getLogger(PushService.class);

    @Autowired
    private JsonToTextUtil jsonToTextUtil;

    @Autowired
    private RestHelper restHelper;

    public void postPushEventToSlack(Object payload) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        String payloadString = mapper.writeValueAsString(payload);
        JSONObject obj = new JSONObject(payloadString);

        // decide if the events represents a branch creation
        boolean before = isAllZero(obj.getString("before"));
        boolean after = isAllZero(obj.getString("after"));

        if (before || after) {
            postBranchEventToSlack(payload, before, after);
            return;
        }

        // parse push events payload and construct notifications that will be post to slack channel
        int commitsNum = obj.getJSONArray("commits").length();
        String headCommitterName = obj.getJSONObject("head_commit").getJSONObject("committer").getString("name");
        String headCommitterNameUrl = obj.getJSONObject("sender").getString("html_url");
        String headCommitterInfoLink = "<" + headCommitterNameUrl + "|@" + headCommitterName + ">";

        String repoUrl = obj.getJSONObject("repository").getString("html_url");
        String repoFullName = obj.getJSONObject("repository").getString("full_name");
        String commitInfo = commitsNum + " new commit(s) by " + headCommitterInfoLink;
        String textAndLink = "[<" + repoUrl + "|" + repoFullName + ">] " + commitInfo;

        List<String> commitList = new ArrayList<>();
        JSONArray commitsArray = obj.getJSONArray("commits");
        for (int i = 0; i < commitsArray.length(); ++i) {
            JSONObject commit = commitsArray.getJSONObject(i);

            // generate commit summary rows for one commit
            Map<String, String> affectedFilesMap = getAffectedFileMap(commit, obj.getString("ref"), repoUrl, repoFullName);

            String commitUrl = commit.getString("url");
            String commitHash = commit.getString("id");

            int len = commitHash.length() >= 8 ? 8 : commitHash.length() - 1;
            commitHash = commitHash.substring(0, len);

            String commitMessage = commit.getString("message");
            String commitAndLink = "<" + commitUrl + "|" + commitHash + ">  -" + commitMessage;
            commitAndLink = commitAndLink + " @" + commit.getJSONObject("committer").getString("name");

            StringBuilder affectedFileRows = new StringBuilder();
            for (String key : affectedFilesMap.keySet()) {
                affectedFileRows.append(key).append(" : ").append(affectedFilesMap.get(key)).append("\n");
            }
            commitList.add(commitAndLink + "\n" + affectedFileRows.toString());
        }

        // build json and post notification messages to slack channel
        JSONObject json = jsonToTextUtil.toJSONPushCommit("GitHub", textAndLink, commitList);

        ResponseEntity<String> response = restHelper.sendRestCall(json);

        if (response.getStatusCode() == HttpStatus.OK) {
            logger.info(json.toString());
        }
    }

    private String generateFileUrl(String branch, String repoUrl, String file) {

        String branchName = branch.startsWith("refs/heads/") ?
                branch.substring(11) : branch;

        String fileUrl = repoUrl + "/blob/" + branchName + "/" + file;

        return "<" + fileUrl + "|" + file + ">";
    }

    private String generateDownloadUrl(String branch, String repoUrl, String file) {

        String branchName = branch.startsWith("refs/heads/") ?
                branch.substring(11) : branch;

        String rawFileUrl = Constants.rawFileUrlPrefix +
                repoUrl.substring(18) + "/" + branchName + "/" + file;

        String eventServiceUrl = Constants.appGeneralPrefix + "/file/?url=";

        return "<" + eventServiceUrl + rawFileUrl + "|download>";
    }

    private String generateChangeUrl(JSONObject commit, String file, String repoFullName) {

        String sha = commit.getString("id");

        String viewChangeServiceUrl = Constants.appGeneralPrefix + "/view?sha=";

        viewChangeServiceUrl = viewChangeServiceUrl + sha + "&repo=" + repoFullName + "&file=" + file;
        return "<" + viewChangeServiceUrl + "|diff>";
    }

    private Map<String, String> getAffectedFileMap(JSONObject commit, String branch, String repoUrl, String repoFullName) {
        Map<String, String> map = new HashMap<>();
        String[] changeTypes = {"added", "removed", "modified"};

        for (String type : changeTypes) {
            JSONArray addedArray = commit.getJSONArray(type);
            if (addedArray.length() != 0) {
                StringBuilder sb = new StringBuilder();
                for (Object str : addedArray) {
                    String fileLink = (String) str;
                    String downloadUrl = " ";
                    String fileChangeUrl = " ";
                    if (!type.equals("removed")) {
                        fileLink = generateFileUrl(branch, repoUrl, (String) str);
                        downloadUrl = generateDownloadUrl(branch, repoUrl, (String) str);
                        fileChangeUrl = generateChangeUrl(commit, (String) str, repoFullName);
                    }
                    sb.append(fileLink).append(" ").append(downloadUrl).append(" ").append(fileChangeUrl).append(" | ");
                }
                map.put(type, sb.toString().trim());
            }
        }

        return map;
    }

    /**
     * Process new branch events
     *
     * @param payload
     * @throws Exception
     */
    private void postBranchEventToSlack(Object payload, boolean before, boolean after) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        String payloadString = mapper.writeValueAsString(payload);
        JSONObject obj = new JSONObject(payloadString);

        String branchUrl = obj.getString("compare");
        String branchName = obj.getString("ref");
        String branchNameAndLink = "<" + branchUrl + "|" + branchName + ">";

        String userName = obj.getJSONObject("sender").getString("login");
        String userUrl = obj.getJSONObject("sender").getString("html_url");
        String userNameAndLink = "<" + userUrl + "|@" + userName + ">";

        JSONObject repoObj = obj.getJSONObject("repository");
        String repoFullName = repoObj.getString("full_name");
        String repoUrl = repoObj.getString("html_url");
        String repoNameAndLink = "[<" + repoUrl + "|" + repoFullName + ">]";

        String branchVerbText = "";
        if (before && !after) {
            branchVerbText = " created new branch";
        }
        if (!before && after) {
            branchVerbText = " deleted branch";
        }

        String branchText = repoNameAndLink + " " + userNameAndLink + branchVerbText;

        JSONObject json = jsonToTextUtil.toJSONIssueComment("GitHub", branchText, branchNameAndLink, "#ffff66");

        ResponseEntity<String> response = restHelper.sendRestCall(json);

        if (response.getStatusCode() == HttpStatus.OK) {
            logger.info(json.toString());
        }
    }

    /*
        check if commit sha is all zero
     */
    private boolean isAllZero(String hash) {

        boolean flag = true;
        for (char c : hash.toCharArray()) {
            if (c != '0') {
                flag = false;
                break;
            }
        }

        return flag;
    }

}
