/** 向日葵：每十秒产生一个可点击的阳光。 */
public class Sunflower extends Plant {
    private int productionTime;
    public Sunflower(int row, int col) { super(row, col, "graphics/Plants/SunFlower/1.gif"); }
    @Override public void update(GameModel game, int elapsedMs) {
        productionTime += elapsedMs;
        if (productionTime >= 10000) {
            productionTime = 0;
            game.addSun(x + 12, y + 20);
            react();
        }
    }
}
