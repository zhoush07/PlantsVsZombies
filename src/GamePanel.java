import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GamePanel extends JPanel {
    private static final int TOTAL_ZOMBIES = 5;

    private final BufferedImage background;
    private final Peashooter peashooter;

    private final List<Zombie> zombies = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();

    private final Timer timer;

    private int spawnedCount = 0;
    private int defeatedCount = 0;

    private int spawnTime = 0;
    private int shootTime = 0;

    private boolean gameOver = false;
    private String resultText = "";

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

        peashooter = new Peashooter(300, 200);

        // 开局先出现一只僵尸
        spawnZombie();

        timer = new Timer(20, e -> {
            updateGame();
            repaint();
        });
    }

    private void spawnZombie() {
        zombies.add(new Zombie(1000, 155));
        spawnedCount++;
    }

    private void updateGame() {
        if (gameOver) {
            return;
        }

        // 1. 每隔 4 秒出现一只僵尸，最多 5 只
        spawnTime += 20;

        if (spawnTime >= 4000 && spawnedCount < TOTAL_ZOMBIES) {
            spawnZombie();
            spawnTime = 0;
        }

        // 2. 场上有僵尸时，每隔 0.8 秒发射一颗豌豆
        shootTime += 20;

        if (!zombies.isEmpty() && shootTime >= 800) {
            bullets.add(new Bullet(355, 215));
            shootTime = 0;
        }

        // 3. 移动僵尸和子弹
        for (Zombie zombie : zombies) {
            zombie.move();
        }

        for (Bullet bullet : bullets) {
            bullet.move();
        }

        // 4. 检查子弹是否打中了僵尸
        Iterator<Bullet> bulletIterator = bullets.iterator();

        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();

            if (bullet.isOffScreen()) {
                bulletIterator.remove();
                continue;
            }

            for (Zombie zombie : zombies) {
                if (!zombie.isDead()
                        && bullet.getBounds().intersects(
                        zombie.getBounds())) {

                    zombie.takeDamage(20);
                    bulletIterator.remove();
                    break;
                }
            }
        }

        // 5. 移除已经被消灭的僵尸
        Iterator<Zombie> zombieIterator = zombies.iterator();

        while (zombieIterator.hasNext()) {
            Zombie zombie = zombieIterator.next();

            if (zombie.isDead()) {
                zombieIterator.remove();
                defeatedCount++;
            }
        }

        // 6. 有僵尸越过草坪左侧，就失败
        for (Zombie zombie : zombies) {
            if (zombie.hasReachedHouse()) {
                finishGame("游戏失败！");
                return;
            }
        }

        // 7. 全部出场，而且全部被消灭，才算胜利
        if (spawnedCount == TOTAL_ZOMBIES && zombies.isEmpty()) {
            finishGame("游戏胜利！");
        }
    }

    private void finishGame(String message) {
        gameOver = true;
        resultText = message;
        timer.stop();
    }

    @Override
    public void addNotify() {
        super.addNotify();

        if (!gameOver) {
            timer.start();
        }
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 先画背景，再画角色和子弹
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        peashooter.draw(g, this);

        for (Zombie zombie : zombies) {
            zombie.draw(g);
        }

        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }

        // 左上方显示进度
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRoundRect(15, 15, 330, 45, 12, 12);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        g.drawString(
                "已出场：" + spawnedCount + "/" + TOTAL_ZOMBIES
                        + "    已消灭：" + defeatedCount,
                30, 44);

        // 游戏结束后显示结果
        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.WHITE);
            g.setFont(new Font("Microsoft YaHei", Font.BOLD, 48));

            FontMetrics metrics = g.getFontMetrics();
            int textX =
                    (getWidth() - metrics.stringWidth(resultText)) / 2;

            g.drawString(resultText, textX, getHeight() / 2);
        }
    }
}