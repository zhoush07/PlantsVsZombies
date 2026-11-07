import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** 管理游戏规则和场上对象，不负责 Swing 窗口。 */
public class GameModel {
    public static final int WIDTH = 1120, HEIGHT = 480;
    public static final int ROWS = 5, COLS = 9;
    public static final int GRID_X = 210, GRID_Y = 80, CELL_W = 65, CELL_H = 75;
    public static final int STEP = 20, TOTAL_ZOMBIES = 10;
    final Plant[][] plants = new Plant[ROWS][COLS];
    final List<Enemy> enemies = new ArrayList<>();
    final List<Shot> shots = new ArrayList<>();
    final List<Sun> suns = new ArrayList<>();
    final List<Blast> blasts = new ArrayList<>();
    final List<AnimationEffect> effects = new ArrayList<>();
    final int[] cooldowns = new int[PlantType.values().length];
    private final Random random = new Random();
    PlantType selectedPlant;
    boolean shovelSelected, gameOver;
    int sunshine = 500, spawnedCount, defeatedCount;
    private int spawnTime, skySunTime;
    String tip = "先选择植物卡片，再点击草坪格子";

    public static int plantY(int row) { return GRID_Y + row * CELL_H + 5; }
    public void selectShovel() {
        if (gameOver) return;
        shovelSelected = !shovelSelected;
        selectedPlant = null;
        tip = shovelSelected ? "点击植物将其铲除，不返还阳光" : "已取消铲子";
    }
    public void addShot(int row, Bullet bullet) { shots.add(new Shot(row, bullet)); }
    public void addSun(int x, int y) { suns.add(new Sun(x, y)); }
    public boolean hasTarget(int row, int muzzleX) {
        return enemies.stream().anyMatch(
                e -> e.row == row
                        && !e.zombie.isDead()
                        && e.zombie.getBounds().getMaxX() >= muzzleX);
    }
    public boolean hasNearbyEnemy(int row, int centerX, int range) {
        return enemies.stream().anyMatch(e -> e.row == row && !e.zombie.isDead()
                && Math.abs(e.zombie.getBounds().getCenterX() - centerX) <= range);
    }
    public void removePlant(Plant plant) {
        if (plants[plant.row][plant.col] == plant) plants[plant.row][plant.col] = null;
    }
    public void explode(int row, int centerX, int centerY, int rowRange, int range) {
        for (Enemy enemy : enemies) {
            if (!enemy.zombie.isDead() && Math.abs(enemy.row - row) <= rowRange
                    && Math.abs(enemy.zombie.getBounds().getCenterX() - centerX) <= range) {
                enemy.zombie.takeDamage(1800);
            }
        }
        blasts.add(new Blast(centerX, centerY, range, rowRange == 0 ? 35 : CELL_H + 35));
    }
    private void finishGame(String message) {
        gameOver = true;
        selectedPlant = null;
        shovelSelected = false;
        tip = message + " 重新运行 Main 可以再玩一局。";
    }


    public void selectPlant(PlantType type) {
        if (gameOver) {
            return;
        }

        shovelSelected = false;

        if (selectedPlant == type) {
            selectedPlant = null;
            tip = "已取消选择";
        } else if (cooldowns[type.ordinal()] > 0) {
            selectedPlant = null;
            tip = type.displayName + "还在冷却";
        } else if (sunshine < type.cost) {
            selectedPlant = null;
            tip = "阳光不足，需要 " + type.cost + " 阳光";
        } else {
            selectedPlant = type;
            tip = "已选择" + type.displayName + "，请点击空格子";
        }



    }

    public void handleClick(int x, int y) {
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
        plants[row][col] = type.create(row, col);
        sunshine -= type.cost;
        cooldowns[type.ordinal()] = type.cooldown;
        selectedPlant = null;

        tip = type.displayName + "种植成功";
    }

    // 只检查同一行，优先寻找右侧遇到的植物
    private Plant findBlockingPlant(Enemy enemy) {
        double zombieX = enemy.zombie.getBounds().getX();

        for (int col = COLS - 1; col >= 0; col--) {
            Plant plant = plants[enemy.row][col];

            if (plant != null
                    && !plant.isDead()
                    && zombieX <= plant.getX() + 30
                    && zombieX + 85 >= plant.getX()) {
                return plant;
            }
        }

        return null;
    }

    public void update() {
        effects.removeIf(effect -> { effect.update(STEP); return effect.isFinished(); });
        blasts.removeIf(blast -> { blast.age += STEP; return blast.isFinished(); });
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

        // 首只僵尸在 10 秒后出现，之后每隔 10 秒出现一只
        spawnTime += STEP;
        if (spawnTime >= 10000 && spawnedCount < TOTAL_ZOMBIES) {
            int row = random.nextInt(ROWS);

            // 僵尸脚底与这一行的植物对齐
            int zombieY = plantY(row) - 45;
            // 三种僵尸依次出场，所在行仍然随机
            Zombie zombie = switch (spawnedCount % 3) {
                case 1 -> new ConeheadZombie(1000, zombieY);
                case 2 -> new BucketheadZombie(1000, zombieY);
                default -> new NormalZombie(1000, zombieY);
            };

            enemies.add(new Enemy(row, zombie));

            spawnedCount++;
            spawnTime = 0;
        }

        // 更新每一株植物
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Plant plant = plants[row][col];
                if (plant != null) {
                    plant.advanceAnimation(STEP);
                    plant.update(this, STEP);
                }
            }
        }

        for (Enemy enemy : enemies) {
            if (enemy.zombie.isDead()) {
                continue;
            }

            Plant target = findBlockingPlant(enemy);

            // 找到植物就啃咬，否则向左移动
            enemy.zombie.update(target, STEP);

            // 被吃掉的植物从格子中移除
            if (target != null && target.isDead()) {
                effects.add(target.deathEffect());
                removePlant(target);
            }
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
                effects.add(enemy.zombie.deathEffect());
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


}