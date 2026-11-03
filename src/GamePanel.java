import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GamePanel extends JPanel {
    private final BufferedImage background;
    private final Peashooter peashooter;

    public GamePanel() {
        setPreferredSize(new Dimension(1120, 480));

        File imageFile = new File("graphics/Map/map0.jpg");

        try {
            background = ImageIO.read(imageFile);

            if (background == null) {
                throw new IOException("图片格式无法读取");
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "加载草坪图片失败，请检查路径："
                            + imageFile.getAbsolutePath(), e);
        }

        // 把一株豌豆射手放在草坪上
        peashooter = new Peashooter(300, 200);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 先画背景
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);

        // 再画植物，让植物显示在背景上面
        peashooter.draw(g, this);
    }
}