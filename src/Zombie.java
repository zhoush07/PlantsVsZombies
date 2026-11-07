import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Zombie {
    private double x;
    private final int y;

    private final int maxHealth;
    private int health;
    private int slowTime;

    private int biteTime;
    private Plant target;

    private final GifAnimation walkAnimation;
    private final GifAnimation attackAnimation;
    private final GifAnimation normalWalk = GifAnimation.load("graphics/Zombies/Zombie/Zombie.gif");
    private final GifAnimation normalAttack = GifAnimation.load("graphics/Zombies/Zombie/ZombieAttack.gif");
    private long animationTime;
    private boolean attacking;
    private boolean exploded;
    private int hitTime;
    public AnimationEffect deathEffect() {
        return new AnimationEffect(exploded
                ? "graphics/Zombies/Zombie/BoomDie.gif"
                : "graphics/Zombies/Zombie/ZombieDie.gif",
                (int)x, y, 85, 110);
    }

    public Zombie(int x, int y) {
        this(x, y, 100, "graphics/Zombies/Zombie/0.gif");
    }

    protected Zombie(int x, int y, int health, String path) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.maxHealth = health;
        String directory = path.substring(0, path.lastIndexOf('/') + 1);
        String type = directory.substring(0, directory.length() - 1);
        type = type.substring(type.lastIndexOf('/') + 1);
        walkAnimation = GifAnimation.load(directory + type + ".gif");
        attackAnimation = GifAnimation.load(directory + type + "Attack.gif");
    }

    // 有植物就停下来啃咬，没有植物就继续走
    public void update(Plant plant, int elapsedMs) {
        if (isDead()) {
            return;
        }

        hitTime = Math.max(0, hitTime - elapsedMs);
        boolean newAttack = plant != null && !plant.isDead();
        if (newAttack != attacking) { attacking = newAttack; animationTime = 0; }
        animationTime += slowTime > 0 ? elapsedMs / 2 : elapsedMs;

        // 换了目标，重新开始计算啃咬时间
        if (plant != target) {
            target = plant;
            biteTime = 0;
        }

        if (target != null && !target.isDead()) {
            biteTime += elapsedMs;

            // 每隔一秒造成 25 点伤害
            while (biteTime >= 1000 && !target.isDead()) {
                target.takeDamage(25);
                SoundManager.play(SoundManager.Sound.BITE);
                biteTime -= 1000;
            }
        } else {
            target = null;
            biteTime = 0;

            double speed = slowTime > 0 ? 15.0 : 30.0;
            x -= speed * elapsedMs / 1000.0;
        }

        // 停下来啃咬时，减速时间也正常流逝
        slowTime = Math.max(0, slowTime - elapsedMs);
    }

    public void move() {
        update(null, 20);
    }

    public void applySlow() {
        slowTime = 3000;
    }

    public void takeDamage(int damage) {
        if (isDead()) return;
        health = Math.max(0, health - damage);
        hitTime = 140;
        if (isDead()) exploded = damage >= 1800;
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
        if (isDead()) return;
        boolean armorLost = maxHealth > 100 && health <= 100;
        GifAnimation animation = armorLost
                ? (attacking ? normalAttack : normalWalk)
                : (attacking ? attackAnimation : walkAnimation);
        g.drawImage(animation.frame(animationTime), (int)x, y, 85, 110, null);
        if (hitTime > 0) {
            g.setColor(new Color(255, 230, 130, hitTime));
            int size = 8 + (140 - hitTime) / 10;
            g.drawOval((int)x + 20 - size/2, y + 45 - size/2, size, size);
        }

        g.setColor(Color.DARK_GRAY);
        g.fillRect((int) x + 15, y - 10, 50, 6);

        g.setColor(slowTime > 0 ? Color.CYAN : Color.RED);
        g.fillRect(
                (int) x + 15, y - 10,
                50 * health / maxHealth, 6);

    }
}