package pizza.kkomdae.dto.respond;

import lombok.Getter;
import pizza.kkomdae.entity.Photo;

@Getter
public class PhotoWithUrl {
    private String name;
    private String aiName;
    private int type;
    private String url;
    private String aiUrl;

    public PhotoWithUrl(Photo photo, String url, String presignedAiUrl) {
        this.name = photo.getName();
        this.aiName = photo.getAiName();
        this.type = photo.getType();
        this.url = url;
        this.aiUrl = presignedAiUrl;
    }

}
