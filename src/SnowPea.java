/** 寒冰射手：复用射击周期，只改变子弹类型。 */
public class SnowPea extends Peashooter {
    public SnowPea(int row, int col) { super(row, col, "graphics/Plants/SnowPea/1.gif"); }
    @Override protected void fire(GameModel game) {
        react();game.addShot(row, new Bullet(x + 55, y + 15, true));
    }
}
