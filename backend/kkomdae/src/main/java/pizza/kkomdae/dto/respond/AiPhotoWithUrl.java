package pizza.kkomdae.dto.respond;

import lombok.Getter;
import pizza.kkomdae.entity.Photo;
import pizza.kkomdae.enums.PhotoType;

@Getter
public class AiPhotoWithUrl {
    private String aiName;
    private int type;
    private String url;
    private int damage;

    public AiPhotoWithUrl(Photo photo, String url) {
        this.aiName = photo.getAiName();
        this.type = photo.getType();
        this.url = url;
        this.damage = photo.getDamage();
    }

}
