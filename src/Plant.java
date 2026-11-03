import java.awt.Graphics;
import java.awt.image.BufferedImage;

/** 所有植物的共同父类；各子类自己实现行为。 */
public abstract class Plant {
    public final int row, col;
    protected final int x, y;
    private final BufferedImage image;
    private boolean consumed;

    protected Plant(int row, int col, String imagePath) {
        this.row = row;
        this.col = col;
        x = GameModel.GRID_X + col * GameModel.CELL_W;
        y = GameModel.plantY(row);
        image = Assets.load(imagePath);
    }

    public abstract void update(GameModel game, int elapsedMs);

    public void draw(Graphics g) {
        g.drawImage(image, x, y, 65, 65, null);
    }

    protected final void explode(GameModel game, int rowRange, int horizontalRange) {
        if (consumed) return;
        consumed = true;
        game.explode(row, x + 32, y + 32, rowRange, horizontalRange);
        game.removePlant(this);
    }
}
