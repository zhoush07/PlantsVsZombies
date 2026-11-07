import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import org.w3c.dom.Node;

/** 读取并合成 GIF 帧，保留透明背景；按游戏时间播放。 */
public class GifAnimation {
    private static final Map<String, GifAnimation> CACHE = new HashMap<>();
    private final java.util.List<BufferedImage> frames = new ArrayList<>();
    private final java.util.List<Integer> delays = new ArrayList<>();
    private int duration;

    public static GifAnimation load(String path) {
        return CACHE.computeIfAbsent(path, GifAnimation::new);
    }
    private static Node child(Node parent, String name) {
        for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling())
            if (n.getNodeName().equals(name)) return n;
        return null;
    }
    private static String attr(Node n, String key) {
        return n.getAttributes().getNamedItem(key).getNodeValue();
    }
    private static BufferedImage copy(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics(); g.drawImage(image, 0, 0, null); g.dispose();
        return result;
    }
    private GifAnimation(String path) {
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        try (ImageInputStream input = ImageIO.createImageInputStream(new File(path))) {
            if (input == null) throw new IllegalStateException("找不到 " + path);
            reader.setInput(input);
            Node screen = child(reader.getStreamMetadata().getAsTree("javax_imageio_gif_stream_1.0"), "LogicalScreenDescriptor");
            BufferedImage canvas = new BufferedImage(Integer.parseInt(attr(screen,"logicalScreenWidth")),
                    Integer.parseInt(attr(screen,"logicalScreenHeight")), BufferedImage.TYPE_INT_ARGB);
            for (int i=0; i<reader.getNumImages(true); i++) {
                Node root = reader.getImageMetadata(i).getAsTree("javax_imageio_gif_image_1.0");
                Node descriptor = child(root,"ImageDescriptor"), control = child(root,"GraphicControlExtension");
                int x=Integer.parseInt(attr(descriptor,"imageLeftPosition"));
                int y=Integer.parseInt(attr(descriptor,"imageTopPosition"));
                BufferedImage before = copy(canvas), frame = reader.read(i);
                Graphics2D g = canvas.createGraphics(); g.drawImage(frame,x,y,null); g.dispose();
                frames.add(copy(canvas));
                int delay=control==null?100:Math.max(20,Integer.parseInt(attr(control,"delayTime"))*10);
                delays.add(delay); duration+=delay;
                String disposal=control==null?"none":attr(control,"disposalMethod");
                if (disposal.equals("restoreToPrevious")) canvas=before;
                else if (disposal.equals("restoreToBackgroundColor")) {
                    g=canvas.createGraphics(); g.setComposite(AlphaComposite.Clear);
                    g.fillRect(x,y,frame.getWidth(),frame.getHeight()); g.dispose();
                }
            }
        } catch (Exception e) { throw new IllegalStateException("GIF加载失败："+path,e); }
        finally { reader.dispose(); }
    }
    public BufferedImage frame(long time) {
        long remaining=time%duration;
        for(int i=0;i<frames.size();i++) {
            if(remaining<delays.get(i)) return frames.get(i);
            remaining-=delays.get(i);
        }
        return frames.get(0);
    }
}
