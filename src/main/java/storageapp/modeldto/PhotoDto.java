package storageapp.modeldto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PhotoDto {

    private String name;
    private String creationTime;
    private String lastModified;
    private Long size;
    private String downloadUri;
    private String fileType;

}