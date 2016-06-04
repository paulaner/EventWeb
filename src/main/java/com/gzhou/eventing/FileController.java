package com.gzhou.eventing;

import com.gzhou.eventing.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping(value = "")
public class FileController {

    @Autowired
    private FileService fileService;

    @RequestMapping(value = "/event/file", method = RequestMethod.GET)
    public void doDownload(HttpServletRequest request,
                           HttpServletResponse response,
                           @RequestParam(value = "url") String url) throws IOException {

        fileService.downloadFile(request, response, url);
    }

}