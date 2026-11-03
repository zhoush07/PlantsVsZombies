/** 普通射手：每 800 毫秒攻击一次。 */
public class Peashooter extends Plant {
    private int attackTime;
    public Peashooter(int row, int col) {
        this(row, col, "graphics/Plants/Peashooter/0.gif");
    }
    protected Peashooter(int row, int col, String path) { super(row, col, path); }
    @Override public void update(GameModel game, int elapsedMs) {
        attackTime = Math.min(800, attackTime + elapsedMs);
        if (attackTime >= 800 && game.hasTarget(row, x + 55)) {
            fire(game);
            attackTime = 0;
        }
    }
    protected void fire(GameModel game) {
        game.addShot(row, new Bullet(x + 55, y + 15));
    }
}
