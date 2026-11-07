import java.awt.*;
public class Blast {
        final int x, y, radiusX, radiusY;
        int age;
        private final GifAnimation animation;
        final int duration;
        boolean isFinished() { return age >= duration; }
        Blast(int x, int y, int radiusX, int radiusY) {
            this.x = x; this.y = y;
            this.radiusX = radiusX; this.radiusY = radiusY;
            animation = GifAnimation.load(radiusY == 35
                    ? "graphics/Plants/PotatoMine/ExplosionSpudow.gif"
                    : "graphics/Plants/CherryBomb/Boom.gif");
            duration = Math.max(600, animation.getDuration());
        }
    void draw(Graphics original) {
        Graphics2D g = (Graphics2D) original.create();
        try {
            float fade = Math.min(1f, (duration - age) / 200f);
            g.setComposite(AlphaComposite.SrcOver.derive(Math.max(0, fade)));
            double scale = Math.min(1, 0.6 + age / 300.0);
            int w = (int)(radiusX * 2 * scale), h = (int)(radiusY * 2 * scale);
            g.drawImage(animation.frameOnce(age), x-w/2, y-h/2, w, h, null);
        } finally { g.dispose(); }
    }
    }