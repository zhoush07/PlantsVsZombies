import javax.swing.*;
import java.awt.*;

/**
 * 管理菜单和游戏之间的切换。
 * 每次重新开始，都创建全新的一局。
 */
public class GameController extends JPanel {
    private GamePanel currentGame;

    public GameController() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1120, 650));
        showMenu();
    }

    private void closeCurrentGame() {
        if (currentGame != null) {
            currentGame.stopGame();
            currentGame = null;
        }

        removeAll();
    }

    public void showMenu() {
        closeCurrentGame();

        add(new MainMenuPanel(this::startGame), BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    public void startGame() {
        closeCurrentGame();

        GamePanel game = new GamePanel();
        currentGame = game;

        JPanel top = new JPanel(new BorderLayout());
        JPanel navigation =
                new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));

        JButton pause = new JButton("暂停");
        JButton restart = new JButton("重新开始");
        JButton menu = new JButton("返回主菜单");

        navigation.add(pause);
        navigation.add(restart);
        navigation.add(menu);

        pause.addActionListener(e -> {
            game.setPaused(!game.isPaused());
            pause.setText(game.isPaused() ? "继续" : "暂停");
        });

        restart.addActionListener(e -> startGame());
        menu.addActionListener(e -> showMenu());

        game.setOnFinished(() -> {
            pause.setEnabled(false);
            pause.setText("本局结束");
        });

        top.add(navigation, BorderLayout.NORTH);
        top.add(game.getToolbar(), BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(game, BorderLayout.CENTER);
        add(game.getStatusBar(), BorderLayout.SOUTH);

        revalidate();
        repaint();
    }
}