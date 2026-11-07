import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public abstract class Plant {
    public final int row, col;
    protected final int x, y;

    private final BufferedImage image;
    private boolean consumed;
    private int health = 100;

    protected Plant(int row, int col, String imagePath) {
        this.row = row;
        this.col = col;

        x = GameModel.GRID_X + col * GameModel.CELL_W;
        y = GameModel.plantY(row);

        image = Assets.load(imagePath);
    }

    public abstract void update(GameModel game, int elapsedMs);

    public void takeDamage(int damage) {
        health = Math.max(0, health - damage);
    }

    public boolean isDead() {
        return health == 0;
    }

    public int getX() {
        return x;
    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, 65, 65, null);

        // 受伤后才显示植物血条
        if (health < 100) {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(x + 7, y - 7, 50, 5);

            g.setColor(Color.GREEN);
            g.fillRect(x + 7, y - 7, health / 2, 5);
        }
    }

    protected final void explode(
            GameModel game, int rowRange, int horizontalRange) {

        if (consumed) {
            return;
        }

        consumed = true;
        game.explode(
                row, x + 32, y + 32,
                rowRange, horizontalRange);

        game.removePlant(this);
    }
}