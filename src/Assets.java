import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** 图片只加载一次，并保留透明背景。 */
public final class Assets {
    private static final Map<String, BufferedImage> CACHE = new HashMap<>();
    private Assets() {}
    public static BufferedImage load(String path) {
        return CACHE.computeIfAbsent(path, key -> {
            try {
                BufferedImage image = ImageIO.read(new File(key));
                if (image == null) throw new IOException("图片格式无法读取");
                return image;
            } catch (IOException e) {
                throw new IllegalStateException("加载图片失败：" + new File(key).getAbsolutePath(), e);
            }
        });
    }
}
