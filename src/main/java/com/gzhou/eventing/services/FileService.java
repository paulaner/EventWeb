package com.gzhou.eventing.services;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


@Service
public class FileService {

    private static final int BUFFER_SIZE = 4096;

    public void downloadFile(HttpServletRequest request,
                             HttpServletResponse response,
                             String fileURL) throws IOException {

        String fileName = FilenameUtils.getName(fileURL);

        ServletContext context = request.getSession().getServletContext();
        String appPath = context.getRealPath("");

        // fetch raw file into local machine
        URL oracle = new URL(fileURL);
        URLConnection yc = oracle.openConnection();
        File targetFile = new File(appPath + fileName);
        FileUtils.copyInputStreamToFile(yc.getInputStream(), targetFile);

        String fullPath = appPath + fileName;
        File downloadFile = new File(fullPath);
        FileInputStream inputStream = new FileInputStream(downloadFile);

        // get MIME type of the file
        String mimeType = context.getMimeType(fullPath);
        if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }

        // set content attributes for the response
        response.setContentType(mimeType);
        response.setContentLength((int) downloadFile.length());

        // set headers for the response
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"",
                downloadFile.getName());
        response.setHeader(headerKey, headerValue);

        // get output stream of the response
        OutputStream outStream = response.getOutputStream();

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;

        // write bytes read from the input stream into the output stream
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outStream.close();

        //clean local file
        boolean deleted = targetFile.delete();
    }

}
