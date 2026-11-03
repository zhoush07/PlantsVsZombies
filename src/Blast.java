import java.awt.*;
public class Blast {
        final int x, y, radiusX, radiusY;
        int age;
        Blast(int x, int y, int radiusX, int radiusY) {
            this.x = x; this.y = y;
            this.radiusX = radiusX; this.radiusY = radiusY;
        }
        void draw(Graphics g) {
            int alpha = Math.max(0, 190 * (600 - age) / 600);
            g.setColor(new Color(255, 110, 20, alpha));
            g.fillOval(x - radiusX, y - radiusY, radiusX * 2, radiusY * 2);
            g.setColor(new Color(255, 240, 100, alpha));
            g.fillOval(x - radiusX / 2, y - radiusY / 2, radiusX, radiusY);
        }
    }