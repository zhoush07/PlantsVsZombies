import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GamePanel extends JPanel {
    private final BufferedImage background;

    public GamePanel() {
        // 原图是 1400 × 600，这里等比例缩小显示
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
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 将草坪图片画满整个游戏面板
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}