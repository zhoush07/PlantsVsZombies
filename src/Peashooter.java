import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;

public class Peashooter {
    private final int x;
    private final int y;
    private final Image image;

    public Peashooter(int x, int y) {
        this.x = x;
        this.y = y;

        ImageIcon icon =
                new ImageIcon("graphics/Plants/Peashooter/0.gif");

        if (icon.getIconWidth() <= 0) {
            throw new IllegalStateException("找不到豌豆射手图片，请检查素材路径");
        }

        image = icon.getImage();
    }

    public void draw(Graphics g, ImageObserver observer) {
        g.drawImage(image, x, y, 65, 65, observer);
    }
}