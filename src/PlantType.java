import java.util.function.BiFunction;

/** 卡片登记表：名称、价格、冷却时间以及对应植物类。 */
public enum PlantType {
    PEA("豌豆射手", 100, 5000, Peashooter::new),
    SUNFLOWER("向日葵", 50, 5000, Sunflower::new),
    REPEATER("双发射手", 200, 5000, Repeater::new),
    SNOW("寒冰射手", 175, 5000, SnowPea::new),
    CHERRY("樱桃炸弹", 150, 20000, CherryBomb::new),
    POTATO("土豆雷", 25, 15000, PotatoMine::new);

    public final String displayName;
    public final int cost, cooldown;
    private final BiFunction<Integer, Integer, Plant> factory;
    PlantType(String name, int cost, int cooldown, BiFunction<Integer,Integer,Plant> factory) {
        displayName = name; this.cost = cost; this.cooldown = cooldown; this.factory = factory;
    }
    public Plant create(int row, int col) { return factory.apply(row, col); }
}
