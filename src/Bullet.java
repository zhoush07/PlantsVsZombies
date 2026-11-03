import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Bullet {
    private int x;
    private final int y;

    public Bullet(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move() {
        x += 8;
    }

    public boolean isOffScreen() {
        return x > 1120;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 16, 16);
    }

    public void draw(Graphics g) {
        g.setColor(new Color(35, 100, 15));
        g.fillOval(x, y, 16, 16);

        g.setColor(new Color(145, 230, 50));
        g.fillOval(x + 2, y + 2, 11, 11);
    }
}