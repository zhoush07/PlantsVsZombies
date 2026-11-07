import java.awt.*;
import java.awt.image.BufferedImage;

/** 短暂视觉效果，不参与碰撞或胜负判断。 */
public class AnimationEffect {
    private final GifAnimation animation;
    private final BufferedImage still;
    private final int x, y, width, height, duration;
    private int age;

    public AnimationEffect(String path, int x, int y, int width, int height) {
        animation = GifAnimation.load(path);
        still = null;
        this.x=x; this.y=y; this.width=width; this.height=height;
        duration=animation.getDuration();
    }
    public AnimationEffect(BufferedImage still, int x, int y, int width, int height) {
        animation=null; this.still=still;
        this.x=x; this.y=y; this.width=width; this.height=height;
        duration=350;
    }
    public void update(int elapsedMs) { age += elapsedMs; }
    public boolean isFinished() { return age >= duration; }
    public void draw(Graphics original) {
        Graphics2D g=(Graphics2D)original.create();
        try {
            float opacity = still == null ? 1f : Math.max(0,1f-age/(float)duration);
            g.setComposite(AlphaComposite.SrcOver.derive(opacity));
            g.drawImage(still == null ? animation.frameOnce(age) : still,
                    x, y + (still == null ? 0 : age/30), width, height, null);
        } finally { g.dispose(); }
    }
}
