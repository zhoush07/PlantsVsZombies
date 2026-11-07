import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel {
    public MainMenuPanel(Runnable startGame, Runnable showEncyclopedia,
                         JComboBox<Difficulty> difficulty, JToggleButton mute) {
        setLayout(new GridBagLayout());
        setBackground(new Color(225,238,203));
        JPanel content=new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
        JLabel title=new JLabel("植物大战僵尸");
        title.setFont(new Font("Microsoft YaHei",Font.BOLD,40));
        JButton start=new JButton("开始游戏");
        JButton encyclopedia=new JButton("植物与僵尸图鉴");
        start.addActionListener(e->{SoundManager.play(SoundManager.Sound.CLICK);startGame.run();});
        encyclopedia.addActionListener(e->{SoundManager.play(SoundManager.Sound.CLICK);showEncyclopedia.run();});
        JLabel description=new JLabel();
        Runnable update=()->description.setText(((Difficulty)difficulty.getSelectedItem()).description());
        difficulty.addActionListener(e->update.run());update.run();
        JLabel label=new JLabel("选择难度（建议先试轻松）");
        for(JComponent component:new JComponent[]{title,start,encyclopedia,label,difficulty,description,mute}){
            component.setAlignmentX(CENTER_ALIGNMENT);
            if(component!=title)component.setFont(new Font("Microsoft YaHei",Font.PLAIN,18));
            content.add(component);content.add(Box.createVerticalStrut(18));
        }
        difficulty.setMaximumSize(new Dimension(220,36));
        add(content);
    }
}