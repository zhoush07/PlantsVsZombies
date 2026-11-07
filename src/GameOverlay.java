import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

/** 覆盖在草坪上，居中显示暂停和胜负结果，并拦截草坪点击。 */
public class GameOverlay extends JPanel {
    private final JLabel title = new JLabel();
    private final JLabel detail = new JLabel();
    private final JButton resume = new JButton("继续游戏");

    private Runnable continueAction = () -> {};
    public void setContinueAction(Runnable action) { continueAction = action; }

    public GameOverlay(Runnable restart, Runnable menu) {
        setOpaque(false);
        setLayout(new GridBagLayout());
        addMouseListener(new MouseAdapter() {});
        JPanel box = new JPanel();
        box.setBackground(new Color(250, 246, 225));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(75,110,50), 3),
                BorderFactory.createEmptyBorder(25, 35, 25, 35)));
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        title.setFont(new Font("Microsoft YaHei", Font.BOLD, 36));
        detail.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
        title.setAlignmentX(CENTER_ALIGNMENT);
        detail.setAlignmentX(CENTER_ALIGNMENT);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        actions.setOpaque(false);
        JButton retry = new JButton("重新开始");
        JButton back = new JButton("返回主菜单");
        resume.addActionListener(e -> continueAction.run());
        retry.addActionListener(e -> restart.run());
        back.addActionListener(e -> menu.run());
        for (JButton button : new JButton[]{resume,retry,back}) {
            button.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
            actions.add(button);
        }
        box.add(title);
        box.add(Box.createVerticalStrut(14));
        box.add(detail);
        box.add(Box.createVerticalStrut(25));
        box.add(actions);
        add(box);
        setVisible(false);
    }
    public void showPause() {
        title.setText("游戏已暂停");
        detail.setText("所有游戏计时已停止，继续后从原处恢复");
        resume.setVisible(true);
        setVisible(true);
        revalidate();
    }
    public void showResult(boolean won) {
        title.setText(won ? "游戏胜利！" : "游戏失败！");
        detail.setText(won ? "你消灭了本关全部僵尸。" : "有僵尸进入了房子，再试一次吧。");
        resume.setVisible(false);
        setVisible(true);
        revalidate();
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(0,0,0,135));
        g.fillRect(0,0,getWidth(),getHeight());
    }
}
