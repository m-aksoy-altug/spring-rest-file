package com.springfile.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

@RestController
@RequestMapping("/files")
public class FileTransferController {
	
	private static final Logger log= LoggerFactory.getLogger(FileTransferController.class);
	
	 private static final String UPLOAD_DIR = "src/main/resources/uploads/";
	
	@PostMapping("/uploadMultiple")
	public ResponseEntity<String> uploadFiles(@RequestParam("files") MultipartFile[] files) throws IOException {
	    if (files.length == 0) {
	        return ResponseEntity.badRequest().body("No files provided!");
	    }
	    
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
	    List<String> fileNames = new ArrayList<>();

	    for (MultipartFile file : files) {
	        if (!file.isEmpty()) {
	            Path path = Paths.get(UPLOAD_DIR + file.getOriginalFilename());
	            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
	            fileNames.add(file.getOriginalFilename());
	        }
	    }

	    return ResponseEntity.ok("Uploaded files: " + String.join(", ", fileNames));
	}
	
	@PostMapping("/uploadZip")
	public ResponseEntity<String> uploadZip(@RequestParam("file") MultipartFile zipFile) throws IOException {
		
		if (zipFile.isEmpty()) {
            return ResponseEntity.badRequest().body("No zip file provided!");
        }
	    
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path zipPath = uploadPath.resolve(zipFile.getOriginalFilename());
        Files.copy(zipFile.getInputStream(), zipPath, StandardCopyOption.REPLACE_EXISTING);

        extractZipFile(zipPath.toString(), UPLOAD_DIR);

        Files.delete(zipPath);

        return ResponseEntity.ok("ZIP file extracted successfully!");
	}
	
	private void extractZipFile(String zipFilePath, String destDir) throws IOException {
        File destDirectory = new File(destDir);
        if (!destDirectory.exists()) {
            destDirectory.mkdirs();
        }

        try (ZipInputStream zipInputStream = 
        		new ZipInputStream(Files.newInputStream(Paths.get(zipFilePath)))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                Path newPath = Paths.get(destDir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    Files.createDirectories(newPath.getParent());
                    Files.copy(zipInputStream, newPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zipInputStream.closeEntry();
            }
        }
    }

	
	@PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        Path path = Paths.get(UPLOAD_DIR + file.getOriginalFilename());
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        return ResponseEntity.ok("File uploaded successfully!");
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) throws IOException {
        Path path = Paths.get(UPLOAD_DIR + filename);
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(resource);
    }
}
