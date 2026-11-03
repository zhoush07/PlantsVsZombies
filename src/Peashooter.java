import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;

public class Peashooter {
    private final int x;
    private final int y;
    private final BufferedImage image;

    public Peashooter(int x, int y) {
        this.x = x;
        this.y = y;

        try {
            image = ImageIO.read(
                    new File("graphics/Plants/Peashooter/0.gif"));

            if (image == null) {
                throw new IOException("无法读取图片");
            }
        } catch (IOException e) {
            throw new IllegalStateException("加载豌豆射手图片失败", e);
        }
    }

    public void draw(Graphics g, ImageObserver observer) {
        g.drawImage(image, x, y, 65, 65, observer);
    }
}