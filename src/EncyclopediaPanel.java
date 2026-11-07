import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** 六种植物和三种僵尸的介绍页；卡片价格和冷却读取实际配置。 */
public class EncyclopediaPanel extends JPanel {
    private record Entry(String name, String image, String description) {
        @Override public String toString() { return name; }
    }
    private final List<Entry> entries = new ArrayList<>();
    private final JLabel picture = new JLabel("", SwingConstants.CENTER);
    private final JTextArea description = new JTextArea();

    public EncyclopediaPanel(Runnable back) {
        setLayout(new BorderLayout(18,18));
        setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        setBackground(new Color(235,242,218));
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel title = new JLabel("植物与僵尸图鉴");
        title.setFont(new Font("Microsoft YaHei", Font.BOLD, 28));
        JButton returnButton = new JButton("返回主菜单");
        returnButton.addActionListener(e -> { SoundManager.play(SoundManager.Sound.CLICK); back.run(); });
        heading.add(title, BorderLayout.WEST);
        heading.add(returnButton, BorderLayout.EAST);
        add(heading, BorderLayout.NORTH);
        String[] folders={"Peashooter","SunFlower","Repeater","SnowPea","CherryBomb","PotatoMine"};
        String[] behaviors={
                "普通豌豆每颗伤害20；每0.8秒攻击一次，只攻击本行前方目标。",
                "每10秒产生一个阳光，点击后获得25阳光。不会攻击。",
                "每0.8秒进行一轮攻击；两颗豌豆间隔0.16秒，每颗伤害20。",
                "每0.8秒发射寒冰豌豆，伤害20。命中后移动速度减半3秒，重复命中刷新时间。啃咬伤害频率不变。",
                "种下1秒后爆炸，对附近本行及上下相邻行造成1800伤害，随后消失。",
                "准备10秒后就绪。同一行僵尸靠近时触发，造成1800范围伤害，随后消失；准备期间可以被吃掉。"
        };
        PlantType[] types=PlantType.values();
        for(int i=0;i<types.length;i++) {
            PlantType t=types[i];
            entries.add(new Entry("植物 · "+t.displayName,"graphics/Plants/"+folders[i]+"/0.gif",
                    t.displayName+"\n\n阳光费用："+t.cost+"\n卡片冷却："+t.cooldown/1000+" 秒"
                            +"\n生命值：100\n\n"+behaviors[i]));
        }
        zombie("普通僵尸","Zombie",100,"没有额外防护，需要5颗普通豌豆才能消灭。");
        zombie("路障僵尸","ConeheadZombie",200,"需要10颗普通豌豆才能消灭。剩余100血时路障消失，变成普通僵尸外观。");
        zombie("铁桶僵尸","BucketheadZombie",400,"需要20颗普通豌豆才能消灭。剩余100血时铁桶消失，变成普通僵尸外观。");
        JList<Entry> list=new JList<>(entries.toArray(new Entry[0]));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFont(new Font("Microsoft YaHei",Font.PLAIN,18));
        list.setFixedCellHeight(42);
        JScrollPane names=new JScrollPane(list);
        names.setPreferredSize(new Dimension(240,400));
        add(names,BorderLayout.WEST);
        JPanel body=new JPanel(new BorderLayout(12,12));
        body.setOpaque(false);
        picture.setPreferredSize(new Dimension(180,180));
        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setFont(new Font("Microsoft YaHei",Font.PLAIN,20));
        description.setMargin(new Insets(15,15,15,15));
        body.add(picture,BorderLayout.NORTH);
        body.add(new JScrollPane(description),BorderLayout.CENTER);
        add(body,BorderLayout.CENTER);
        list.addListSelectionListener(e -> {
            if(e.getValueIsAdjusting() || list.getSelectedValue()==null) return;
            Entry entry=list.getSelectedValue();
            java.awt.image.BufferedImage image=Assets.load(entry.image());
            double scale=Math.min(160.0/image.getWidth(),160.0/image.getHeight());
            picture.setIcon(new ImageIcon(image.getScaledInstance(
                    Math.max(1,(int)(image.getWidth()*scale)),
                    Math.max(1,(int)(image.getHeight()*scale)),Image.SCALE_SMOOTH)));
            description.setText(entry.description());
            description.setCaretPosition(0);
        });
        list.setSelectedIndex(0);
    }
    private void zombie(String name,String folder,int health,String special) {
        entries.add(new Entry("僵尸 · "+name,"graphics/Zombies/"+folder+"/0.gif",
                name+"\n\n生命值："+health+"\n移动速度：每秒30像素（游戏内部坐标）"
                        +"\n啃咬：每秒25点伤害\n\n"+special
                        +"\n\n遇到本行植物会停下啃咬；植物消失后继续移动。"));
    }
}
