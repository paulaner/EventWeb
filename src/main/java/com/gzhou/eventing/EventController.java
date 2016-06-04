package com.gzhou.eventing;

import com.gzhou.eventing.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "")
public class EventController {

    @Autowired
    private PullRequestService pullRequestService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private PushService pushService;

    @Autowired
    private IssueService issueService;

    @Autowired
    private ForkService forkService;

    /**
     * Response to any push events coming from git webhook, and post push/commits notifications to slack channel
     * @param payload events from git, example see: @Link https://developer.github.com/v3/activity/events/types/#pushevent
     * @throws Exception
     */
    @RequestMapping(value = "/event/push", method = RequestMethod.POST)
    @ResponseBody
    public void postPushEventToSlack(@RequestBody Object payload) throws Exception {

        pushService.postPushEventToSlack(payload);
    }

    /**
     * Response to issue comments events and post to slack channel
     * @param payload issue comments events payload from git, example see: @Link https://developer.github.com/v3/activity/events/types/#issuecommentevent
     * @throws Exception
     */
    @RequestMapping(value = "/event/issuecomment", method = RequestMethod.POST)
    @ResponseBody
    public void postIssueCommentEventToSlack(@RequestBody Object payload) throws Exception {

        issueService.postIssueCommentEventToSlack(payload);
    }


    /**
     * Post issue related notifications to slack
     * @param payload issue events from git, example see: @Link https://developer.github.com/v3/activity/events/types/#issuesevent
     * @throws Exception
     */
    @RequestMapping(value = "/event/issue", method = RequestMethod.POST)
    @ResponseBody
    public void postIssueEventToSlack(@RequestBody Object payload) throws Exception {

        issueService.postIssueEventToSlack(payload);
    }


    /**
     * Post pull requests notifications to slack
     * @param payload pull request events, example see: @Link https://developer.github.com/v3/activity/events/types/#pullrequestevent
     * @throws Exception
     */
    @RequestMapping(value = "/event/pullrequest", method = RequestMethod.POST)
    @ResponseBody
    public void postPullRequestEventToSlack(@RequestBody Object payload) throws Exception {

        pullRequestService.postPullRequestEventToSlack(payload);
    }

    /**
     * Post fork events to slack, NOTE: this feature not fully functioning yet, in debugging.
     * @param payload example see: @Link https://developer.github.com/v3/activity/events/types/#forkevent
     * @throws Exception
     */
    @RequestMapping(value = "/event/fork", method = RequestMethod.POST)
    @ResponseBody
    public void postForkEventToSlack(@RequestBody Object payload) throws Exception {

        forkService.postForkEventToSlack(payload);
    }

    /**
     * Post repo events to slack, NOTE: this feature not fully functioning yet, in debugging.
     * @param payload example see: @Link https://developer.github.com/v3/activity/events/types/#repositoryevent
     * @throws Exception
     */
    @RequestMapping(value = "/event/repo", method = RequestMethod.POST)
    @ResponseBody
    public void postRepositoryEventToSlack(@RequestBody Object payload) throws Exception {

        repositoryService.postRepoEventToSlack(payload);
    }

}