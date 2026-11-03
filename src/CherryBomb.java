/** 樱桃炸弹：一秒后对附近三行造成爆炸伤害，然后移除自己。 */
public class CherryBomb extends Plant {
    private int fuseTime;
    public CherryBomb(int row, int col) { super(row, col, "graphics/Plants/CherryBomb/0.gif"); }
    @Override public void update(GameModel game, int elapsedMs) {
        fuseTime += elapsedMs;
        if (fuseTime >= 1000) explode(game, 1, GameModel.CELL_W * 3 / 2);
    }
}
