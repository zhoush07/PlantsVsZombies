import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel {
    public MainMenuPanel(Runnable startGame) {
        setLayout(new GridBagLayout());
        setBackground(new Color(225, 238, 203));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("植物大战僵尸");
        title.setFont(new Font("Microsoft YaHei", Font.BOLD, 40));
        title.setAlignmentX(CENTER_ALIGNMENT);

        JButton start = new JButton("开始游戏");
        start.setFont(new Font("Microsoft YaHei", Font.BOLD, 24));
        start.setAlignmentX(CENTER_ALIGNMENT);
        start.addActionListener(e -> startGame.run());

        JLabel hint = new JLabel("种植植物，收集阳光，阻止僵尸进入房子");
        hint.setFont(new Font("Microsoft YaHei", Font.PLAIN, 17));
        hint.setAlignmentX(CENTER_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(35));
        content.add(start);
        content.add(Box.createVerticalStrut(25));
        content.add(hint);

        add(content);
    }
}