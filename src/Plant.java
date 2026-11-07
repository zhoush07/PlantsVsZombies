import java.awt.Color;
import java.awt.Graphics;

public abstract class Plant {
    public final int row, col;
    protected final int x, y;

    private GifAnimation animation;
    private long animationTime;
    private int reactionTime;

    public final void advanceAnimation(int elapsedMs) {
        animationTime += elapsedMs;
        reactionTime = Math.max(0, reactionTime - elapsedMs);
    }
    protected final void react() { reactionTime = 160; }
    protected final void setAnimation(String path) {
        GifAnimation next = GifAnimation.load(path);
        if (next != animation) { animation = next; animationTime = 0; }
    }
    public final AnimationEffect deathEffect() {
        return new AnimationEffect(animation.frame(animationTime), x, y, 65, 65);
    }
    private boolean consumed;
    private int health = 100;

    protected Plant(int row, int col, String imagePath) {
        this.row = row;
        this.col = col;

        x = GameModel.GRID_X + col * GameModel.CELL_W;
        y = GameModel.plantY(row);

        animation = GifAnimation.load(imagePath);
    }

    public abstract void update(GameModel game, int elapsedMs);

    public void takeDamage(int damage) {
        health = Math.max(0, health - damage);
        react();
    }

    public boolean isDead() {
        return health == 0;
    }

    public int getX() {
        return x;
    }

    public void draw(Graphics g) {
        int recoil = reactionTime * 4 / 160;
        g.drawImage(animation.frame(animationTime), x - recoil, y, 65, 65, null);

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