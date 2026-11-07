import java.awt.*;
public class Sun {
        final int x;
        final int y;
        int age;

        Sun(int x, int y) {
            this.x = x;
            this.y = y;
        }

    private int bob() { return (int)(Math.sin(age / 300.0) * 4); }
        boolean contains(int mouseX, int mouseY) {
            int dx = mouseX - (x + 22);
            int dy = mouseY - (y + 22 + bob());
            return dx * dx + dy * dy <= 22 * 22;
        }

    void draw(Graphics original) {
            Graphics2D g = (Graphics2D) original.create();
            g.translate(0, bob());
            g.setColor(new Color(255, 210, 20, 160));
            for (int i = 0; i < 8; i++) {
                double angle = age / 800.0 + i * Math.PI / 4;
                int rx = x + 22 + (int)(Math.cos(angle) * 25);
                int ry = y + 22 + (int)(Math.sin(angle) * 25);
                g.fillOval(rx - 3, ry - 3, 6, 6);
            }
            g.setColor(new Color(255, 170, 0));
            g.fillOval(x, y, 44, 44);

            g.setColor(Color.YELLOW);
            g.fillOval(x + 4, y + 4, 36, 36);

            g.setColor(new Color(120, 70, 0));
            g.setFont(new Font("Microsoft YaHei", Font.BOLD, 15));
            g.drawString("+25", x + 7, y + 28);
            g.dispose();
        }
    }