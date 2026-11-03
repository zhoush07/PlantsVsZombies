import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Zombie {
    // 所有僵尸共用同一张图片，不用每次重新读取
    private static final BufferedImage IMAGE = loadImage();

    private double x;
    private final int y;
    private int health = 100;
    private int slowTime;

    public Zombie(int x, int y) {
        this.x = x;
        this.y = y;
    }

    private static BufferedImage loadImage() {
        try {
            BufferedImage image = ImageIO.read(
                    new File("graphics/Zombies/Zombie/0.gif"));

            if (image == null) {
                throw new IOException("无法读取图片");
            }

            return image;
        } catch (IOException e) {
            throw new IllegalStateException("加载僵尸图片失败", e);
        }
    }

    public void move() {
        x -= slowTime > 0 ? 0.3 : 0.6;
        slowTime = Math.max(0, slowTime - 20);
    }

    public void applySlow() {
        slowTime = 3000; // 重复命中刷新时间，不叠加减速强度
    }

    public void takeDamage(int damage) {
        health = Math.max(0, health - damage);
    }

    public boolean isDead() {
        return health == 0;
    }

    public boolean hasReachedHouse() {
        return x < 200;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, y, 85, 110);
    }

    public void draw(Graphics g) {
        g.drawImage(IMAGE, (int) x, y, 85, 110, null);

        // 僵尸头顶的小血条
        g.setColor(Color.DARK_GRAY);
        g.fillRect((int) x + 15, y - 10, 50, 6);

        g.setColor(slowTime > 0 ? Color.CYAN : Color.RED);
        g.fillRect((int) x + 15, y - 10, health / 2, 6);
    }
}