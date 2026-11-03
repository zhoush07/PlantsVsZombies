import java.awt.*;
public class Sun {
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