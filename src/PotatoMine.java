import java.awt.Color;
import java.awt.Graphics;

/** 土豆雷：准备十秒后，等待同一行僵尸靠近。 */
public class PotatoMine extends Plant {
    private int preparationTime;
    public PotatoMine(int row, int col) { super(row, col, "graphics/Plants/PotatoMine/0.gif"); }
    @Override public void update(GameModel game, int elapsedMs) {
        preparationTime = Math.min(10000, preparationTime + elapsedMs);
        if (preparationTime >= 10000 && game.hasNearbyEnemy(row, x + 32, 45)) {
            explode(game, 0, 65);
        }
    }
    @Override public void draw(Graphics g) {
        super.draw(g);
        g.setColor(preparationTime < 10000 ? Color.ORANGE : Color.GREEN);
        g.fillOval(x + 48, y, 10, 10);
        if (preparationTime < 10000) {
            g.setColor(new Color(90, 60, 20));
            g.fillRect(x + 5, y + 65, 55, 4);
            g.setColor(Color.ORANGE);
            g.fillRect(x + 5, y + 65, 55 * preparationTime / 10000, 4);
        }
    }
}
