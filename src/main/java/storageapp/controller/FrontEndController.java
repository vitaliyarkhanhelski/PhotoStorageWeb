package storageapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import storageapp.service.PhotoService;

@Controller
public class FrontEndController {

    private PhotoService photoService;

    public FrontEndController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @GetMapping("/admin/gallery")
    public String adminGallery(ModelMap map) {
        map.put("files", photoService.getPhotos());
        return "gallery_admin";
    }


    @GetMapping("/")
    public String login() {
        return "login";
    }

//    @GetMapping("/admin")
//    public String admin() {
//        return "admin";
//    }

    @GetMapping("/gallery")
    public String userGallery(ModelMap map) {
        map.put("files", photoService.getPhotos());
        return "gallery";
    }

}