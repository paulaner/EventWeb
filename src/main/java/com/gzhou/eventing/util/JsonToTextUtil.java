package com.gzhou.eventing.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;

/*
    Util class that contains methods of construct different json object
 */
@Component
public class JsonToTextUtil {


    public JSONObject toJSONTitleAndOneLine(String username, String text,
                                            String issueTitle,
                                            String issueLink, String issueBody, String color) {

        JSONObject json = new JSONObject();
        json.put("username", username);
        if (text != null) {
            json.put("text", text);
        }

        JSONArray fieldsArray = new JSONArray();
        JSONObject fieldRow = new JSONObject();
        fieldRow.put("value", issueBody);
        fieldRow.put("short", false);
        fieldsArray.put(fieldRow);

        JSONObject attachmentsObj = new JSONObject();
        attachmentsObj.put("color", color);
        if (issueTitle != null && issueLink != null) {
            attachmentsObj.put("title", issueTitle);
            attachmentsObj.put("title_link", issueLink);
        }
        attachmentsObj.put("fields", fieldsArray);

        JSONArray attachmentsArray = new JSONArray();
        attachmentsArray.put(attachmentsObj);

        json.put("attachments", attachmentsArray);

        return json;
    }

    public JSONObject toJSONPushCommit(String username, String text, List<String> commits) {
        JSONArray attachmentsArray = new JSONArray();

        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("text", text);

        JSONObject attachmentsObj = new JSONObject();
        attachmentsObj.put("color", "#36a64f");

        JSONArray fieldsArray = new JSONArray();

        for (String commit : commits) {
            JSONObject fieldRow = new JSONObject();
            fieldRow.put("value", commit);
            fieldRow.put("short", false);

            fieldsArray.put(fieldRow);
        }

        attachmentsObj.put("fields", fieldsArray);

        attachmentsArray.put(attachmentsObj);

        json.put("attachments", attachmentsArray);

        return json;
    }

    public JSONObject toJSONIssueComment(String username, String text, String body, String color) {

        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("text", text);

        JSONArray fieldsArray = new JSONArray();
        JSONObject fieldRow = new JSONObject();
        fieldRow.put("value", body);
        fieldRow.put("short", false);
        fieldsArray.put(fieldRow);

        JSONObject attachmentsObj = new JSONObject();
        attachmentsObj.put("color", color);
        attachmentsObj.put("fields", fieldsArray);

        JSONArray attachmentsArray = new JSONArray();
        attachmentsArray.put(attachmentsObj);

        json.put("attachments", attachmentsArray);

        return json;
    }

    public JSONObject toJSONIssue(String username, String text, String issueTitle,
                                  String issueLink, String issueBody, String color) {

        JSONObject json = new JSONObject();
        json.put("username", username);
        if (text != null) {
            json.put("text", text);
        }

        JSONArray fieldsArray = new JSONArray();
        JSONObject fieldRow = new JSONObject();
        fieldRow.put("value", issueBody);
        fieldRow.put("short", false);
        fieldsArray.put(fieldRow);

        JSONObject attachmentsObj = new JSONObject();
        attachmentsObj.put("color", color);
        if (issueTitle != null && issueLink != null) {
            attachmentsObj.put("title", issueTitle);
            attachmentsObj.put("title_link", issueLink);
        }
        attachmentsObj.put("fields", fieldsArray);

        JSONArray attachmentsArray = new JSONArray();
        attachmentsArray.put(attachmentsObj);

        json.put("attachments", attachmentsArray);

        return json;
    }

}
