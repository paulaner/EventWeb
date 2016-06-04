package com.gzhou.eventing;

import com.gzhou.eventing.dto.Change;
import com.gzhou.eventing.services.ViewChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "")
public class ViewChangeController {

    @Autowired
    private ViewChangeService viewChangeService;

    @RequestMapping(value = "/event/view", method = RequestMethod.GET)
    @ResponseBody
    public Change getFileChange(@RequestParam(value = "sha") String sha,
                                @RequestParam(value = "repo") String repo,
                                @RequestParam(value = "file") String file) throws Exception {

        return viewChangeService.getChange(sha, repo, file);
    }

}
