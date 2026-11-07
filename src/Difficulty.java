/** 关卡难度配置；不改变植物价格或角色血量。 */
public enum Difficulty {
    EASY("轻松", 500, 10, 15000, 15000, 15000),
    NORMAL("普通", 350, 15, 20000, 14000, 9000),
    HARD("挑战", 250, 20, 18000, 11000, 6500);

    public final String title;
    public final int initialSun, totalZombies, firstDelay;
    private final int startInterval, minimumInterval;
    Difficulty(String title, int sun, int total, int first, int start, int minimum) {
        this.title=title; initialSun=sun; totalZombies=total;
        firstDelay=first; startInterval=start; minimumInterval=minimum;
    }
    public int spawnDelay(int alreadySpawned) {
        if (alreadySpawned == 0) return firstDelay;
        return Math.max(minimumInterval, startInterval - (alreadySpawned / 3) * 1000);
    }
    public String description() {
        return "初始阳光 " + initialSun + "，共 " + totalZombies
                + " 只僵尸；第一只在 " + firstDelay/1000 + " 秒后出现";
    }
    @Override public String toString() { return title; }
}
