package com.xployt.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class FileUploadUtil {
    private static final Logger logger = CustomLogger.getLogger();

    public static class UploadResult {
        private final Map<String, String> formFields;
        private final List<FileItem> fileItems;
        private final List<File> uploadedFiles;

        public UploadResult(Map<String, String> formFields, List<FileItem> fileItems, List<File> uploadedFiles) {
            this.formFields = formFields;
            this.fileItems = fileItems;
            this.uploadedFiles = uploadedFiles;
        }

        public Map<String, String> getFormFields() {
            return formFields;
        }
        
        public List<FileItem> getFileItems() {
            return fileItems;
        }

        public List<File> getUploadedFiles() {
            return uploadedFiles;
        }

        public String getFormField(String fieldName) {
            return formFields.get(fieldName);
        }
    }

    public static UploadResult processMultipartRequest(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        // Check that we have a file upload request
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request is not multipart");
            return null;
        }

        Map<String, String> formFields = new HashMap<>();
        List<FileItem> fileItems = new ArrayList<>();
        List<File> uploadedFiles = new ArrayList<>();
        
        try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            List<FileItem> items = upload.parseRequest(request);
            
            // Extract form fields and files
            for (FileItem item : items) {
                if (item.isFormField()) {
                    formFields.put(item.getFieldName(), item.getString());
                } else {
                    fileItems.add(item);
                }
            }
            
            return new UploadResult(formFields, fileItems, uploadedFiles);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing multipart request: {0}", e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request");
            return null;
        }
    }
    
    public static List<File> processAttachments(List<FileItem> files, List<String> fileIds, 
                                               ServletContext context, HttpServletResponse response) throws IOException {
        List<File> uploadedFiles = new ArrayList<>();
        
        for (int i = 0; i < files.size(); i++) {
            FileItem item = files.get(i);
            String fileId = fileIds.get(i);

            String fileExtension = "";
            String originalFileName = item.getName();
            int lastDot = originalFileName.lastIndexOf('.');
            if (lastDot > 0) {
                fileExtension = originalFileName.substring(lastDot);
            }
            
            String uploadPath = context.getRealPath("/uploads");
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            File file = new File(uploadDir, fileId + fileExtension);

            try (InputStream inputStream = item.getInputStream()) {
                Path outputPath = file.toPath();
                Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
                uploadedFiles.add(file);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error writing file: {0}", e.getMessage());
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing file upload");
                throw e;
            }
        }
        
        return uploadedFiles;
    }
} 