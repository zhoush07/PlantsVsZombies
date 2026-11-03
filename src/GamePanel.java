import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel {
    // 先开放一行，共 9 个格子
    private static final int GRID_X = 210;
    private static final int GRID_Y = 200;
    private static final int CELL_SIZE = 65;
    private static final int COLUMN_COUNT = 9;

    private static final int PLANT_COST = 100;
    private static final int TOTAL_ZOMBIES = 5;

    private final BufferedImage background;

    // 每个格子最多放一株植物
    private final Peashooter[] plants =
            new Peashooter[COLUMN_COUNT];

    // 每株植物分别记录自己的发射时间
    private final int[] shootTimes = new int[COLUMN_COUNT];

    private final List<Zombie> zombies = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Sun> suns = new ArrayList<>();

    private final Random random = new Random();
    private final Timer timer;



    private int sunshine = 150;
    private boolean plantSelected = false;

    private int spawnedCount = 0;
    private int defeatedCount = 0;
    private int spawnTime = 0;
    private int sunTime = 0;

    private boolean gameOver = false;
    private String resultText = "";
    private String tip = "点击射手卡片，再点击下面的格子种植";

    private final JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
    private final JLabel sunLabel = new JLabel();
    private final JLabel progressLabel = new JLabel();
    private final JLabel statusLabel = new JLabel();
    private final JToggleButton plantButton = new JToggleButton("豌豆射手 · 100 阳光");

    public JPanel getToolbar() { return toolbar; }
    public JLabel getStatusBar() { return statusLabel; }

    private void refreshInterface() {
        sunLabel.setText("阳光：" + sunshine);
        progressLabel.setText("已出场：" + spawnedCount + "/" + TOTAL_ZOMBIES
                + "    已消灭：" + defeatedCount);
        plantButton.setSelected(plantSelected);
        plantButton.setEnabled(!gameOver);
        statusLabel.setText(gameOver ? resultText + " 关闭后重新运行可再玩一局。" : tip);
    }

    private double viewScale() {
        return Math.min(getWidth() / 1120.0, getHeight() / 480.0);
    }

    public GamePanel() {
        setPreferredSize(new Dimension(1120, 480));
        setBackground(new Color(223, 234, 210));
        toolbar.setBackground(new Color(243, 238, 220));
        toolbar.add(sunLabel);
        toolbar.add(plantButton);
        toolbar.add(progressLabel);
        toolbar.add(new JLabel("F11 全屏 / Esc 退出全屏"));
        for (Component component : toolbar.getComponents()) {
            component.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
        }
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(243, 238, 220));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        plantButton.addActionListener(e -> {
            if (gameOver) return;
            if (plantSelected) {
                plantSelected = false;
                tip = "已取消选择";
            } else if (sunshine >= PLANT_COST) {
                plantSelected = true;
                tip = "请选择一个空格子";
            } else {
                tip = "阳光不足，种植需要 100 阳光";
            }
            refreshInterface();
            repaint();
        });
        refreshInterface();

        File imageFile = new File("graphics/Map/map0.jpg");

        try {
            background = ImageIO.read(imageFile);

            if (background == null) {
                throw new IOException("图片格式无法读取");
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "加载草坪图片失败："
                            + imageFile.getAbsolutePath(), e);
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                double scale = viewScale();
                if (scale <= 0) return;
                double offsetX = (getWidth() - 1120 * scale) / 2;
                double offsetY = (getHeight() - 480 * scale) / 2;
                double x = (e.getX() - offsetX) / scale;
                double y = (e.getY() - offsetY) / scale;
                if (x < 0 || x >= 1120 || y < 0 || y >= 480) return;
                handleClick((int) x, (int) y);
                refreshInterface();
            }
        });

        timer = new Timer(20, e -> {
            updateGame();
            refreshInterface();
            repaint();
        });
    }

    private void handleClick(int mouseX, int mouseY) {
        if (gameOver) {
            return;
        }

        // 优先检查是否点击了阳光
        Iterator<Sun> iterator = suns.iterator();

        while (iterator.hasNext()) {
            Sun sun = iterator.next();

            if (sun.contains(mouseX, mouseY)) {
                sunshine += 25;
                iterator.remove();
                tip = "收集成功！阳光 +25";
                repaint();
                return;
            }
        }

        Rectangle plantingArea = new Rectangle(
                GRID_X, GRID_Y,
                COLUMN_COUNT * CELL_SIZE, CELL_SIZE);

        // 检查点击位置是否在种植区域
        if (plantingArea.contains(mouseX, mouseY)) {
            int column = (mouseX - GRID_X) / CELL_SIZE;

            if (!plantSelected) {
                tip = "请先点击左上角的射手卡片";
            } else if (plants[column] != null) {
                tip = "这个格子已经有植物了";
            } else if (sunshine < PLANT_COST) {
                tip = "阳光不足";
            } else {
                int plantX = GRID_X + column * CELL_SIZE;

                plants[column] = new Peashooter(plantX, GRID_Y);
                shootTimes[column] = 0;

                sunshine -= PLANT_COST;
                plantSelected = false;
                tip = "种植成功！";
            }
        } else {
            plantSelected = false;
            tip = "已取消选择，请种在标出的格子里";
        }

        repaint();
    }

    private void updateGame() {
        if (gameOver) {
            return;
        }

        // 每隔 5 秒生成一个可点击的阳光
        sunTime += 20;

        if (sunTime >= 5000) {
            int sunX = GRID_X + random.nextInt(550);
            int sunY = 290 + random.nextInt(110);

            suns.add(new Sun(sunX, sunY));
            sunTime = 0;
        }

        // 阳光出现 12 秒后消失
        Iterator<Sun> sunIterator = suns.iterator();

        while (sunIterator.hasNext()) {
            Sun sun = sunIterator.next();
            sun.age += 20;

            if (sun.age >= 12000) {
                sunIterator.remove();
            }
        }

        // 开局留 8 秒种植物，之后每隔 8 秒生成僵尸
        spawnTime += 20;

        if (spawnTime >= 8000 && spawnedCount < TOTAL_ZOMBIES) {
            zombies.add(new Zombie(1000, 155));
            spawnedCount++;
            spawnTime = 0;
        }

        // 每株植物独立发射子弹
        for (int column = 0; column < COLUMN_COUNT; column++) {
            if (plants[column] == null) {
                continue;
            }

            shootTimes[column] = Math.min(
                    shootTimes[column] + 20, 800);

            int muzzleX = GRID_X + column * CELL_SIZE + 55;

            // 只有前方存在僵尸时才开火
            boolean hasTarget = false;

            for (Zombie zombie : zombies) {
                if (zombie.getBounds().x >= muzzleX) {
                    hasTarget = true;
                    break;
                }
            }

            if (hasTarget && shootTimes[column] >= 800) {
                bullets.add(new Bullet(muzzleX, GRID_Y + 15));
                shootTimes[column] = 0;
            }
        }

        for (Zombie zombie : zombies) {
            zombie.move();
        }

        for (Bullet bullet : bullets) {
            bullet.move();
        }

        // 子弹命中检测
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

        // 清理被消灭的僵尸
        Iterator<Zombie> zombieIterator = zombies.iterator();

        while (zombieIterator.hasNext()) {
            Zombie zombie = zombieIterator.next();

            if (zombie.isDead()) {
                zombieIterator.remove();
                defeatedCount++;
            }
        }

        for (Zombie zombie : zombies) {
            if (zombie.hasReachedHouse()) {
                finishGame("游戏失败！");
                return;
            }
        }

        if (spawnedCount == TOTAL_ZOMBIES && zombies.isEmpty()) {
            finishGame("游戏胜利！");
        }
    }

    private void finishGame(String text) {
        gameOver = true;
        resultText = text;
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
    protected void paintComponent(Graphics original) {
        super.paintComponent(original);
        Graphics2D g = (Graphics2D) original.create();
        double scale = viewScale();
        g.translate((getWidth() - 1120 * scale) / 2,
                (getHeight() - 480 * scale) / 2);
        g.scale(scale, scale);
        g.clipRect(0, 0, 1120, 480);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.drawImage(background, 0, 0, 1120, 480, this);

        // 标出当前可以种植的一行格子
        for (int column = 0; column < COLUMN_COUNT; column++) {
            int cellX = GRID_X + column * CELL_SIZE;

            if (plantSelected && plants[column] == null) {
                g.setColor(new Color(255, 255, 100, 65));
                g.fillRect(cellX, GRID_Y, CELL_SIZE, CELL_SIZE);
            }

            g.setColor(new Color(255, 255, 255, 150));
            g.drawRect(cellX, GRID_Y, CELL_SIZE, CELL_SIZE);
        }

        for (Peashooter plant : plants) {
            if (plant != null) {
                plant.draw(g, this);
            }
        }

        for (Zombie zombie : zombies) {
            zombie.draw(g);
        }

        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }

        for (Sun sun : suns) {
            sun.draw(g);
        }

        g.dispose();
    }

    // 暂时把阳光这个小类放在这里，之后可以单独拆成文件
    private static class Sun {
        private final int x;
        private final int y;
        private int age = 0;

        Sun(int x, int y) {
            this.x = x;
            this.y = y;
        }

        boolean contains(int mouseX, int mouseY) {
            int dx = mouseX - (x + 22);
            int dy = mouseY - (y + 22);
            return dx * dx + dy * dy <= 22 * 22;
        }

        void draw(Graphics g) {
            g.setColor(new Color(255, 170, 0));
            g.fillOval(x, y, 44, 44);

            g.setColor(Color.YELLOW);
            g.fillOval(x + 4, y + 4, 36, 36);

            g.setColor(new Color(120, 70, 0));
            g.setFont(new Font("Microsoft YaHei", Font.BOLD, 15));
            g.drawString("+25", x + 7, y + 28);
        }
    }
}