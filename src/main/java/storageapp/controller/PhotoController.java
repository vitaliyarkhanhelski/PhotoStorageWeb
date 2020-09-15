package storageapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import storageapp.mapper.PhotoMapper;
import storageapp.model.Photo;
import storageapp.model.UploadFileResponse;
import storageapp.modeldto.PhotoDto;
import storageapp.service.PhotoService;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/")
public class PhotoController {

    private PhotoService photoService;
    private PhotoMapper photoMapper;

    public PhotoController(PhotoService photoService, PhotoMapper photoMapper) {
        this.photoService = photoService;
        this.photoMapper = photoMapper;
    }

    @GetMapping("/admin/photos")
    public List<Photo> getPhotosAdmin() {
        return photoService.getPhotos();
    }


    @GetMapping("/photos")
    public List<PhotoDto> getPhotosUser() {
        return photoMapper.convertToListLocalFileDto();
    }


    @GetMapping("photos/download/{f}")
    public ResponseEntity<?> getPhoto(@PathVariable(value="f") String filename) {
        return photoService.getPhoto(filename);
    }


    @GetMapping("photos/delete/{f}")
    public ResponseEntity<?> deletePhoto(@PathVariable(value="f") String filename) {
        return photoService.deletePhoto(filename);
    }


    @PostMapping("photos")
    public ResponseEntity<UploadFileResponse> uploadPhoto(@RequestParam("file") MultipartFile file){
        if (!file.getContentType().equals("image/png") && !file.getContentType().equals("image/jpeg"))
            return new ResponseEntity<>(
                    new UploadFileResponse(file.getOriginalFilename(),
                            "Forbidden type!",
                            file.getContentType()),
                    HttpStatus.FORBIDDEN);
        return photoService.uploadPhoto(file);
    }

}
