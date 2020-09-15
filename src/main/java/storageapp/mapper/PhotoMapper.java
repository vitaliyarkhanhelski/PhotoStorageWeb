package storageapp.mapper;

import com.google.common.collect.FluentIterable;
import org.springframework.stereotype.Component;
import storageapp.service.PhotoService;
import storageapp.modeldto.PhotoDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PhotoMapper {

    private PhotoService photoService;

    public PhotoMapper(PhotoService photoService) {
        this.photoService = photoService;
    }

    public List<PhotoDto> convertToListLocalFileDto() {
        return FluentIterable.from(photoService.getPhotos()).toList()
                .stream()
                .map(p -> new PhotoDto().builder()
                        .name(p.getName())
                        .creationTime(p.getCreationTime())
                        .lastModified(p.getLastModified())
                        .size(p.getSize())
                        .downloadUri(p.getDownloadUri())
                        .fileType(p.getFileType()).build())
                .collect(Collectors.toList());
    }
}
