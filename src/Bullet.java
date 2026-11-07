import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Bullet {
    private int x;
    private final int y;
    private final boolean ice;
    private int age;

    public Bullet(int x, int y) {
        this(x, y, false);
    }

    public Bullet(int x, int y, boolean ice) {
        this.ice = ice;
        this.x = x;
        this.y = y;
    }

    public void hit(Zombie zombie) {
        zombie.takeDamage(20);
        if (ice) zombie.applySlow();
    }

    public void move() {
        x += 8;
        age += 20;
    }

    public boolean isOffScreen() {
        return x > 1120;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 16, 16);
    }

    public void draw(Graphics g) {
        g.setColor(ice ? new Color(30, 100, 180) : new Color(35, 100, 15));
        g.fillOval(x, y, 16, 16);

        g.setColor(ice ? new Color(150, 235, 255) : new Color(145, 230, 50));
        g.fillOval(x + 2, y + 2, 11, 11);
        double angle = age / 110.0;
        g.setColor(ice ? new Color(180, 240, 255, 130) : new Color(160, 240, 70, 100));
        g.fillOval(x - 9, y + 5, 7, 7);
        g.setColor(Color.WHITE);
        g.fillOval(x + 6 + (int)(Math.cos(angle)*3),
                y + 6 + (int)(Math.sin(angle)*3), 3, 3);
    }
}