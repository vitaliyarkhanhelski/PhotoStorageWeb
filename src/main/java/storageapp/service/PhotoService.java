package storageapp.service;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import storageapp.exceptions.ConfigPropertyException;
import storageapp.model.Photo;
import storageapp.model.UploadFileResponse;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static storageapp.config.Constants.*;

@Service
public class PhotoService {

    public static final Logger logger = LoggerFactory.getLogger(PhotoService.class);

    //@Value("${files.path}")
    private String uploads = "./photos/";

    public PhotoService() {
        try {
            createContextDirectory();
        } catch (ConfigPropertyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createContextDirectory() throws ConfigPropertyException, IOException {
        if (Strings.isBlank(uploads)) {
            logger.error("Can't get path files");
            throw new ConfigPropertyException("Can't get path files"); //aplikacja tu sie zatrzyma
        }
        Path path = Paths.get(uploads);
        if (Files.notExists(path))
            try {
                logger.warn("Try to create directory: {}", path);
                Files.createDirectories(path);
                if (Files.exists(path)) logger.info("Folder '{}' creation succeed", path);
            } catch (IOException e) {
                logger.error("Can't create directory: {}, Exception: {}", path, e.getMessage());
                throw new IOException(e.getMessage());
            }
        else logger.info("Folder '{}' exists", path);
    }


    public ResponseEntity<?> getPhoto(String fileName) {
        Resource resource;
        Path path = Paths.get(uploads + fileName);
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            logger.error("Can't get file: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
        File targetFile;
        try {
            targetFile = resource.getFile();
        } catch (IOException e) {
            logger.error("Can't get file: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
        String contentType;
        try {
            contentType = Files.probeContentType(path);
        } catch (IOException e) {
            logger.error("Can't get file: {}", e.getMessage());
            return ResponseEntity
                    .ok()
                    .body(e.getMessage());
        }
        //Cookie cookie = new Cookie("MyCookie", "value667");
        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline") //attachent;filename=\"" + targetFile.getName() + "\"
                .header("Cookie", "ciastkoApp=12345;idvisit=user667")
                .contentLength(targetFile.length())
                .body(resource);
    }


    public List<Photo> getPhotos() {
        Stream<Path> files;
        try {
            logger.info("Try to get all files");
            files = Files.walk(Paths.get(uploads))
                    .filter(Files::isRegularFile);
        } catch (IOException e) {
            logger.error("Can't get files: {}", e.getMessage());
            return null;
        }
        List<Photo> photos = new ArrayList<>();
        files.forEach(
                f -> {
                    BasicFileAttributes bs;
                    try {
                        bs = Files.readAttributes(f, BasicFileAttributes.class);
                    } catch (IOException e) {
                        logger.error("Can't get file attributes: {}", e.getMessage());
                        return;
                    }
                    String downloadURi = ServletUriComponentsBuilder
                            .fromCurrentContextPath()
                            .path(DOWNLOAD_URI)
                            .path(f.getFileName().toString())
                            .toUriString();

                    String deleteUri = ServletUriComponentsBuilder
                            .fromCurrentContextPath()
                            .path(DELETE_URI)
                            .path(f.getFileName().toString())
                            .toUriString();

                    Photo photo = Photo.builder()
                            .name(f.getFileName().toString())
                            .creationTime(bs.creationTime().toString())
                            .lastModified(bs.lastModifiedTime().toString())
                            .size(bs.size())
                            .downloadUri(downloadURi)
                            .deleteUri(deleteUri)
                            .build();
                    try {
                        photo.setFileType(Files.probeContentType(f.toAbsolutePath()));
                    } catch (IOException e) {
                        logger.error("Error while getting probeContentType: {}", e.getMessage());
                        return;
                    }
                    photos.add(photo);
                }
        );
        return photos;
    }


    public ResponseEntity<String> deletePhoto(String filename) {
        Path path = Paths.get(uploads + filename);
        try {
            Files.deleteIfExists(path);
            logger.info("Deleted file: {}", path.getFileName());
            return new ResponseEntity<>("Deleted file: " + path.getFileName(), HttpStatus.OK);
        } catch (IOException e) {
            logger.info("Deleted file: {}, Exception: {}", path.getFileName(), e.getMessage());
            return new ResponseEntity<>("File not found: " + path.getFileName(), HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<UploadFileResponse> uploadPhoto(MultipartFile file) {
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(DOWNLOAD_URI)
                .pathSegment(file.getOriginalFilename())
                .toUriString();

        Path path = Paths.get(uploads + file.getOriginalFilename());
        if (Files.exists(path))
            return new ResponseEntity<>(
                    new UploadFileResponse(file.getOriginalFilename(),
                            fileDownloadUri,
                            file.getContentType()),
                    HttpStatus.FOUND);
        try {
            logger.info("Try to upload file: {}", file.getOriginalFilename());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Can't get file: {} from input: {}", file.getOriginalFilename(), e.getMessage());
            return new ResponseEntity<>(
                    new UploadFileResponse(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(
                new UploadFileResponse(file.getOriginalFilename(),
                        fileDownloadUri,
                        file.getContentType()),
                HttpStatus.CREATED);
    }
}
