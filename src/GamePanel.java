import javax.imageio.ImageIO;
import javax.swing.*;
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
    // 游戏内部始终使用这套坐标，窗口放大时统一缩放
    private static final int WIDTH = 1120;
    private static final int HEIGHT = 480;

    private static final int ROWS = 5;
    private static final int COLS = 9;
    private static final int GRID_X = 210;
    private static final int GRID_Y = 80;
    private static final int CELL_W = 65;
    private static final int CELL_H = 75;

    private static final int TOTAL_ZOMBIES = 10;
    private static final int STEP = 20;
    private static final int CARD_COOLDOWN = 5000;

    private enum PlantType {
        PEA("豌豆射手", 100),
        SUNFLOWER("向日葵", 50), REPEATER("双发射手", 200), SNOW("寒冰射手", 175);

        final String name;
        final int cost;

        PlantType(String name, int cost) {
            this.name = name;
            this.cost = cost;
        }
    }

    private final BufferedImage background =
            loadImage("graphics/Map/map0.jpg");
    private final BufferedImage sunflowerImage =
            loadImage("graphics/Plants/SunFlower/0.gif");

    private final BufferedImage repeaterImage = loadImage("graphics/Plants/Repeater/0.gif");
    private final BufferedImage snowImage = loadImage("graphics/Plants/SnowPea/0.gif");

    private final Plant[][] plants = new Plant[ROWS][COLS];
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Shot> shots = new ArrayList<>();
    private final List<Sun> suns = new ArrayList<>();

    private final Random random = new Random();

    private final JPanel toolbar =
            new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 8));
    private final JLabel sunLabel = new JLabel();
    private final JLabel progressLabel = new JLabel();
    private final JLabel statusLabel = new JLabel();

    private final JToggleButton repeaterButton = new JToggleButton();
    private final JToggleButton snowButton = new JToggleButton();
    private final JToggleButton peaButton = new JToggleButton();
    private final JToggleButton sunflowerButton = new JToggleButton();
    private final JToggleButton shovelButton =
            new JToggleButton("铲子");

    private final int[] cooldowns =
            new int[PlantType.values().length];

    private final Timer timer;

    private PlantType selectedPlant;
    private boolean shovelSelected;

    // 五行版本先给较多的初始阳光，方便测试和布防
    private int sunshine = 500;
    private int spawnedCount;
    private int defeatedCount;
    private int spawnTime;
    private int skySunTime;
    private boolean gameOver;

    private String tip = "先选择植物卡片，再点击草坪格子";

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(223, 234, 210));

        buildInterface();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                double scale = viewScale();
                if (scale <= 0) {
                    return;
                }

                double offsetX = (getWidth() - WIDTH * scale) / 2;
                double offsetY = (getHeight() - HEIGHT * scale) / 2;

                double x = (e.getX() - offsetX) / scale;
                double y = (e.getY() - offsetY) / scale;

                if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
                    return;
                }

                handleClick((int) x, (int) y);
                refreshInterface();
                repaint();
            }
        });

        timer = new Timer(STEP, e -> {
            updateGame();
            refreshInterface();
            repaint();
        });

        refreshInterface();
    }

    private static BufferedImage loadImage(String path) {
        try {
            BufferedImage image = ImageIO.read(new File(path));
            if (image == null) {
                throw new IOException("无法读取图片");
            }
            return image;
        } catch (IOException e) {
            throw new IllegalStateException("图片加载失败：" + path, e);
        }
    }

    public JPanel getToolbar() {
        return toolbar;
    }

    public JLabel getStatusBar() {
        return statusLabel;
    }

    private void buildInterface() {
        toolbar.setBackground(new Color(243, 238, 220));
        toolbar.add(sunLabel);
        toolbar.add(peaButton);
        toolbar.add(sunflowerButton);
        toolbar.add(repeaterButton);
        toolbar.add(snowButton);
        toolbar.add(shovelButton);
        toolbar.add(progressLabel);

        for (Component component : toolbar.getComponents()) {
            component.setFont(
                    new Font("Microsoft YaHei", Font.PLAIN, 14));
        }

        statusLabel.setFont(
                new Font("Microsoft YaHei", Font.PLAIN, 15));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(243, 238, 220));
        statusLabel.setBorder(
                BorderFactory.createEmptyBorder(8, 12, 8, 12));

        repeaterButton.addActionListener(e -> selectPlant(PlantType.REPEATER));
        snowButton.addActionListener(e -> selectPlant(PlantType.SNOW));
        peaButton.addActionListener(e -> selectPlant(PlantType.PEA));
        sunflowerButton.addActionListener(
                e -> selectPlant(PlantType.SUNFLOWER));

        shovelButton.addActionListener(e -> {
            if (gameOver) {
                return;
            }

            shovelSelected = !shovelSelected;
            selectedPlant = null;
            tip = shovelSelected
                    ? "点击植物将其铲除，不返还阳光"
                    : "已取消铲子";

            refreshInterface();
            repaint();
        });
    }

    private void selectPlant(PlantType type) {
        if (gameOver) {
            return;
        }

        shovelSelected = false;

        if (selectedPlant == type) {
            selectedPlant = null;
            tip = "已取消选择";
        } else if (cooldowns[type.ordinal()] > 0) {
            selectedPlant = null;
            tip = type.name + "还在冷却";
        } else if (sunshine < type.cost) {
            selectedPlant = null;
            tip = "阳光不足，需要 " + type.cost + " 阳光";
        } else {
            selectedPlant = type;
            tip = "已选择" + type.name + "，请点击空格子";
        }

        refreshInterface();
        repaint();
    }

    private void refreshInterface() {
        sunLabel.setText("阳光：" + sunshine);
        progressLabel.setText(
                "出场 " + spawnedCount + "/" + TOTAL_ZOMBIES
                        + "  消灭 " + defeatedCount);

        updateCard(repeaterButton, PlantType.REPEATER);
        updateCard(snowButton, PlantType.SNOW);
        updateCard(peaButton, PlantType.PEA);
        updateCard(sunflowerButton, PlantType.SUNFLOWER);

        shovelButton.setSelected(shovelSelected);
        shovelButton.setEnabled(!gameOver);

        statusLabel.setText(tip + "    |    F11 全屏 / Esc 退出全屏");
    }

    private void updateCard(JToggleButton button, PlantType type) {
        int remaining = cooldowns[type.ordinal()];
        String text = type.name + " " + type.cost;

        if (remaining > 0) {
            text += "（" + ((remaining + 999) / 1000) + "秒）";
        }

        button.setText(text);
        button.setSelected(selectedPlant == type);
        button.setEnabled(!gameOver);

        // 保留点击能力，点击时可以提示“阳光不足”或“正在冷却”
        button.setForeground(
                remaining > 0 || sunshine < type.cost
                        ? Color.GRAY : new Color(30, 90, 30));
    }

    private void handleClick(int x, int y) {
        if (gameOver) {
            return;
        }

        // 无论是否选择了植物或铲子，都可以收集阳光
        Iterator<Sun> sunIterator = suns.iterator();

        while (sunIterator.hasNext()) {
            Sun sun = sunIterator.next();

            if (sun.contains(x, y)) {
                sunshine += 25;
                sunIterator.remove();
                tip = "阳光 +25";
                return;
            }
        }

        Rectangle lawn = new Rectangle(
                GRID_X, GRID_Y, COLS * CELL_W, ROWS * CELL_H);

        if (!lawn.contains(x, y)) {
            selectedPlant = null;
            shovelSelected = false;
            tip = "已取消选择";
            return;
        }

        int row = (y - GRID_Y) / CELL_H;
        int col = (x - GRID_X) / CELL_W;

        if (shovelSelected) {
            if (plants[row][col] == null) {
                tip = "这里没有植物";
            } else {
                plants[row][col] = null;
                shovelSelected = false;
                tip = "植物已移除，不返还阳光";
            }
            return;
        }

        if (selectedPlant == null) {
            tip = "请先选择植物卡片";
            return;
        }

        if (plants[row][col] != null) {
            tip = "这个格子已经有植物";
            return;
        }

        if (cooldowns[selectedPlant.ordinal()] > 0) {
            tip = "这张植物卡片还在冷却";
            return;
        }

        if (sunshine < selectedPlant.cost) {
            tip = "阳光不足";
            return;
        }

        PlantType type = selectedPlant;
        plants[row][col] = new Plant(type, row, col);
        sunshine -= type.cost;
        cooldowns[type.ordinal()] = CARD_COOLDOWN;
        selectedPlant = null;

        tip = type.name + "种植成功";
    }

    private void updateGame() {
        if (gameOver) {
            return;
        }

        // 卡片冷却倒计时
        for (int i = 0; i < cooldowns.length; i++) {
            cooldowns[i] = Math.max(0, cooldowns[i] - STEP);
        }

        // 先清理旧阳光
        Iterator<Sun> sunIterator = suns.iterator();
        while (sunIterator.hasNext()) {
            Sun sun = sunIterator.next();
            sun.age += STEP;
            if (sun.age >= 12000) {
                sunIterator.remove();
            }
        }

        // 自然出现的阳光
        skySunTime += STEP;
        if (skySunTime >= 5000) {
            suns.add(new Sun(
                    GRID_X + random.nextInt(540),
                    GRID_Y + random.nextInt(320)));
            skySunTime = 0;
        }

        // 首只僵尸在 15 秒后出现，之后每隔 15 秒出现一只
        spawnTime += STEP;
        if (spawnTime >= 15000 && spawnedCount < TOTAL_ZOMBIES) {
            int row = random.nextInt(ROWS);

            // 僵尸脚底与这一行的植物对齐
            int zombieY = plantY(row) - 45;
            enemies.add(new Enemy(row, new Zombie(1000, zombieY)));

            spawnedCount++;
            spawnTime = 0;
        }

        // 更新每一株植物
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Plant plant = plants[row][col];
                if (plant != null) {
                    plant.update();
                }
            }
        }

        for (Enemy enemy : enemies) {
            enemy.zombie.move();
        }

        for (Shot shot : shots) {
            shot.bullet.move();
        }

        // 子弹只检测同一行的僵尸
        Iterator<Shot> shotIterator = shots.iterator();

        while (shotIterator.hasNext()) {
            Shot shot = shotIterator.next();

            if (shot.bullet.isOffScreen()) {
                shotIterator.remove();
                continue;
            }

            for (Enemy enemy : enemies) {
                if (enemy.row == shot.row
                        && !enemy.zombie.isDead()
                        && shot.bullet.getBounds().intersects(
                        enemy.zombie.getBounds())) {

                    shot.bullet.hit(enemy.zombie);
                    shotIterator.remove();
                    break;
                }
            }
        }

        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();

            if (enemy.zombie.isDead()) {
                enemyIterator.remove();
                defeatedCount++;
            }
        }

        for (Enemy enemy : enemies) {
            if (enemy.zombie.hasReachedHouse()) {
                finishGame("游戏失败！有僵尸进入了房子。");
                return;
            }
        }

        if (spawnedCount == TOTAL_ZOMBIES && enemies.isEmpty()) {
            finishGame("游戏胜利！全部僵尸已消灭。");
        }
    }

    private int plantY(int row) {
        return GRID_Y + row * CELL_H + 5;
    }

    private void finishGame(String message) {
        gameOver = true;
        selectedPlant = null;
        shovelSelected = false;
        tip = message + " 重新运行 Main 可以再玩一局。";
        timer.stop();
    }

    private double viewScale() {
        return Math.min(
                getWidth() / (double) WIDTH,
                getHeight() / (double) HEIGHT);
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
        try {
            double scale = viewScale();
            if (scale <= 0) {
                return;
            }

            g.translate(
                    (getWidth() - WIDTH * scale) / 2,
                    (getHeight() - HEIGHT * scale) / 2);
            g.scale(scale, scale);
            g.clipRect(0, 0, WIDTH, HEIGHT);

            g.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            g.drawImage(background, 0, 0, WIDTH, HEIGHT, this);

            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    int x = GRID_X + col * CELL_W;
                    int y = GRID_Y + row * CELL_H;
                    Plant plant = plants[row][col];

                    if (selectedPlant != null && plant == null) {
                        g.setColor(new Color(255, 255, 100, 55));
                        g.fillRect(x, y, CELL_W, CELL_H);
                    }

                    if (shovelSelected && plant != null) {
                        g.setColor(new Color(255, 100, 60, 90));
                        g.fillRect(x, y, CELL_W, CELL_H);
                    }



                    if (plant != null) {
                        plant.draw(g);
                    }
                }
            }

            for (Enemy enemy : enemies) {
                enemy.zombie.draw(g);
            }

            for (Shot shot : shots) {
                shot.bullet.draw(g);
            }

            // 阳光最后绘制，方便看见并点击
            for (Sun sun : suns) {
                sun.draw(g);
            }
        } finally {
            g.dispose();
        }
    }

    // 暂时放在这里，后面增加更多植物时再拆成独立文件
    private class Plant {
        final PlantType type;
        final int row;
        final int x;
        final int y;
        final Peashooter pea;

        int actionTime;
        int secondShotTime;

        Plant(PlantType type, int row, int col) {
            this.type = type;
            this.row = row;
            this.x = GRID_X + col * CELL_W;
            this.y = plantY(row);
            this.pea = type == PlantType.PEA
                    ? new Peashooter(x, y) : null;
        }

        void update() {
            if (type == PlantType.SUNFLOWER) {
                actionTime += STEP;

                if (actionTime >= 10000) {
                    suns.add(new Sun(x + 12, y + 20));
                    actionTime = 0;
                }
                return;
            }

            // 双发射手的第二颗豌豆在第一颗之后 160 毫秒发射
            if (secondShotTime > 0) {
                secondShotTime -= STEP;
                if (secondShotTime <= 0) {
                    shots.add(new Shot(row, new Bullet(x + 55, y + 15)));
                }
            }
            actionTime = Math.min(actionTime + STEP, 800);

            boolean hasTarget = false;
            for (Enemy enemy : enemies) {
                if (enemy.row == row
                        && !enemy.zombie.isDead()
                        && enemy.zombie.getBounds().x >= x + 55) {
                    hasTarget = true;
                    break;
                }
            }

            if (hasTarget && actionTime >= 800) {
                shots.add(new Shot(row, new Bullet(x + 55, y + 15, type == PlantType.SNOW)));
                if (type == PlantType.REPEATER) {
                    secondShotTime = 160;
                }
                actionTime = 0;
            }
        }

        void draw(Graphics g) {
            if (type == PlantType.PEA) {
                pea.draw(g, GamePanel.this);
            } else {
                g.drawImage(
                        (type == PlantType.REPEATER ? repeaterImage : type == PlantType.SNOW ? snowImage : sunflowerImage), x, y, 65, 65, GamePanel.this);
            }
        }
    }

    // 记录僵尸在哪一行
    private static class Enemy {
        final int row;
        final Zombie zombie;

        Enemy(int row, Zombie zombie) {
            this.row = row;
            this.zombie = zombie;
        }
    }

    // 记录子弹在哪一行
    private static class Shot {
        final int row;
        final Bullet bullet;

        Shot(int row, Bullet bullet) {
            this.row = row;
            this.bullet = bullet;
        }
    }

    private static class Sun {
        final int x;
        final int y;
        int age;

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