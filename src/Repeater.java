/** 双发射手：沿用射手的攻击周期，再延迟 160 毫秒补发一颗。 */
public class Repeater extends Peashooter {
    private int secondShotTime;
    public Repeater(int row, int col) { super(row, col, "graphics/Plants/Repeater/0.gif"); }
    @Override public void update(GameModel game, int elapsedMs) {
        if (secondShotTime > 0) {
            secondShotTime -= elapsedMs;
            if (secondShotTime <= 0) super.fire(game);
        }
        super.update(game, elapsedMs);
    }
    @Override protected void fire(GameModel game) {
        super.fire(game);
        secondShotTime = 160;
    }
}
