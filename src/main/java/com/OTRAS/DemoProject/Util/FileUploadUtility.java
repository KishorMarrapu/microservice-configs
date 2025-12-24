//package com.OTRAS.DemoProject.Util;
// 
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//import org.springframework.stereotype.Component;
//import org.springframework.web.multipart.MultipartFile;
// 
//@Component
//
//public class FileUploadUtility {
// 
//    private static final String BASE_FOLDER = "C://WZGImages/";
//
//    private static final String BASE_URL = "http://localhost:8068/food/";
// 
//    public String uploadFile(MultipartFile file, String folderName) {
//
//        try {
//
//            String folderPath = BASE_FOLDER + "//" + folderName;
//
//            Files.createDirectories(Paths.get(folderPath));
// 
//            String originalFilename = file.getOriginalFilename();
//
//            String uniqueFileName = System.currentTimeMillis() + "_" + originalFilename;
//
//            String fullPath = folderPath + "//" + uniqueFileName;
// 
//            file.transferTo(new File(fullPath));
// 
//            return BASE_URL + folderName + "/" + uniqueFileName;
//
//        } catch (IOException e) {
//
//            throw new RuntimeException("File upload failed: " + e.getMessage());
//
//        }
//
//    }
// 
//
//    public String replaceFile(MultipartFile newFile, String folderName, String oldFileUrl) {
//
//        if (oldFileUrl != null && !oldFileUrl.isBlank()) {
//
//            deleteFile(oldFileUrl);
//
//        }
//
//        return uploadFile(newFile, folderName);
//
//    }
// 
//
//    public void deleteFile(String fileUrl) {
//
//        try {
//
//            if (fileUrl == null || !fileUrl.contains(BASE_URL)) return;
// 
//            String relativePath = fileUrl.replace(BASE_URL, "").replace("/", "//");
//
//            String fullPath = BASE_FOLDER + "//" + relativePath;
//
//            Files.deleteIfExists(Paths.get(fullPath));
//
//        } catch (IOException e) {
//
//            System.err.println("File delete failed: " + e.getMessage());
//
//        }
//
//    }
//
//    public String uploadCandidateDocument(MultipartFile file, Long candidateId, String docType) {
//        String folderName = "candidate_" + candidateId;
//        return uploadFile(file, folderName);
//    }
// 
//    public String uploadFile(MultipartFile file, String subfolder, String baseFileName) throws IOException {
//        if (file.isEmpty()) {
//            throw new IOException("Failed to store empty file.");
//        }
//
//        String originalFilename = file.getOriginalFilename();
//        String fileExtension = "";
//        if (originalFilename != null && originalFilename.contains(".")) {
//            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
//        } else {
//            fileExtension = ".jpg"; 
//        }
//        
//        String uniqueFileName = System.currentTimeMillis() + "_" + baseFileName + fileExtension;
//        
//        Path uploadPath = Paths.get(BASE_FOLDER, subfolder);
//        
//        if (!Files.exists(uploadPath)) {
//            try {
//                Files.createDirectories(uploadPath);
//            } catch (IOException e) {
//                throw new IOException("Could not create upload directory: " + uploadPath, e);
//            }
//        }
//        
//        Path filePath = uploadPath.resolve(uniqueFileName);
//        
//        try {
//            Files.copy(file.getInputStream(), filePath); 
//        } catch (IOException e) {
//            throw new IOException("Failed to store file " + uniqueFileName + ". Error: " + e.getMessage(), e);
//        }
//
//        return subfolder + "/" + uniqueFileName;
//    }
//    
//
//}
//
// 
package com.OTRAS.DemoProject.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUploadUtility {

    //  Linux upload directory
    private static final String BASE_FOLDER = "/opt/tomcat/uploads/";

    //  Linux URL prefix
    private static final String BASE_URL = "http://103.168.19.113:8080/OTRAS_Demo/";

    // ============================================================================================
    // 1) uploadFile(MultipartFile file, String folderName)
    // ============================================================================================
    public String uploadFile(MultipartFile file, String folderName) {

        try {
            String folderPath = BASE_FOLDER + "/" + folderName;

            Files.createDirectories(Paths.get(folderPath));

            String originalFilename = file.getOriginalFilename();
            String uniqueFileName = System.currentTimeMillis() + "_" + originalFilename;

            String fullPath = folderPath + "/" + uniqueFileName;

            file.transferTo(new File(fullPath));

            return BASE_URL + folderName + "/" + uniqueFileName;

        } catch (IOException e) {
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }

    // ============================================================================================
    // 2) replaceFile(...)
    // ============================================================================================
    public String replaceFile(MultipartFile newFile, String folderName, String oldFileUrl) {

        if (oldFileUrl != null && !oldFileUrl.isBlank()) {
            deleteFile(oldFileUrl);
        }

        return uploadFile(newFile, folderName);
    }

    // ============================================================================================
    // 3) deleteFile(...)
    // ============================================================================================
    public void deleteFile(String fileUrl) {

        try {
            if (fileUrl == null || !fileUrl.contains(BASE_URL)) return;

            String relativePath = fileUrl.replace(BASE_URL, "");

            String fullPath = BASE_FOLDER + relativePath;

            Files.deleteIfExists(Paths.get(fullPath));

        } catch (IOException e) {
            System.err.println("File delete failed: " + e.getMessage());
        }
    }

    // ============================================================================================
    // 4) uploadCandidateDocument(MultipartFile file, Long candidateId, String docType)
    //     ✔ YOU NEED THIS — DO NOT DELETE — Controller uses 3 parameters
    // ============================================================================================
    public String uploadCandidateDocument(MultipartFile file, Long candidateId, String docType) {
        String folderName = "candidate_" + candidateId;
        return uploadFile(file, folderName);
    }

    // ============================================================================================
    // 5) uploadFile(file, subfolder, baseFileName)
    //     ✔ YOU NEED THIS — PaymentController uses it
    // ============================================================================================
    public String uploadFile(MultipartFile file, String subfolder, String baseFileName) throws IOException {

        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file.");
        }

        String original = file.getOriginalFilename();
        String ext = "";

        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        } else {
            ext = ".jpg";
        }

        String uniqueFileName = System.currentTimeMillis() + "_" + baseFileName + ext;

        Path uploadPath = Paths.get(BASE_FOLDER, subfolder);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(uniqueFileName);

        Files.copy(file.getInputStream(), filePath);

        return subfolder + "/" + uniqueFileName;
    }
}

