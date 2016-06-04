package com.gzhou.eventing.services;

import com.gzhou.eventing.dto.Change;
import com.gzhou.eventing.util.Constants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ViewChangeService {

    public Change getChange(String sha, String reponame, String file) {

        // call git api to retrieve file change summary
        String fileChangesUrl = Constants.changeUrlPrefix +reponame + "/commits/" + sha;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(fileChangesUrl, HttpMethod.GET, entity, String.class);
        JSONObject filesChangeObj = new JSONObject(responseEntity.getBody());

        JSONArray filesChanges = filesChangeObj.getJSONArray("files");

        // construct file change object
        Change changes = new Change();
        for (int i = 0; i < filesChanges.length(); ++i) {
            JSONObject changeFile = filesChanges.getJSONObject(i);
            String filename = changeFile.getString("filename");
            if (filename.equals(file)) {
                changes = new Change(changeFile.getInt("additions"), changeFile.getInt("deletions"),
                        changeFile.getInt("changes"), changeFile.getString("status"), changeFile.getString("patch"));
                break;
            }
        }

        return changes;
    }
}
