import javax.swing.*;
import java.awt.*;

/**
 * 管理菜单和游戏之间的切换。
 * 每次重新开始，都创建全新的一局。
 */
public class GameController extends JPanel {
    private GamePanel currentGame;
    private Difficulty selectedDifficulty=Difficulty.EASY;

    private JToggleButton muteButton() {
        JToggleButton mute=new JToggleButton();
        mute.setSelected(SoundManager.isMuted());
        mute.setText(SoundManager.isMuted()?"声音：关":"声音：开");
        mute.addActionListener(e->{
            SoundManager.setMuted(mute.isSelected());
            mute.setText(mute.isSelected()?"声音：关":"声音：开");
        });
        return mute;
    }

    public void showEncyclopedia() {
        closeCurrentGame();
        SoundManager.setPaused(false);
        SoundManager.setMusicEnabled(true);
        add(new EncyclopediaPanel(this::showMenu),BorderLayout.CENTER);
        revalidate();repaint();
    }

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
        SoundManager.setPaused(false);
        SoundManager.setMusicEnabled(true);
        JComboBox<Difficulty> difficulty=new JComboBox<>(Difficulty.values());
        difficulty.setSelectedItem(selectedDifficulty);
        difficulty.addActionListener(e->selectedDifficulty=(Difficulty)difficulty.getSelectedItem());
        add(new MainMenuPanel(this::startGame,this::showEncyclopedia,
                difficulty,muteButton()),BorderLayout.CENTER);
        revalidate();repaint();
    }

    public void startGame() {
        closeCurrentGame();
        SoundManager.setPaused(false);
        SoundManager.setMusicEnabled(true);
        GamePanel game=new GamePanel(selectedDifficulty);
        currentGame=game;
        JPanel top=new JPanel(new BorderLayout());
        JPanel navigation=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,5));
        JButton pause=new JButton("暂停");
        JButton restart=new JButton("重新开始");
        JButton menu=new JButton("返回主菜单");
        navigation.add(new JLabel("难度："+selectedDifficulty.title));
        navigation.add(muteButton());
        navigation.add(pause);navigation.add(restart);navigation.add(menu);

        GameOverlay overlay=new GameOverlay(this::startGame,this::showMenu);
        // 继续按钮与顶部按钮共用同一段逻辑。
        Runnable resume=()->{
            game.setPaused(false);SoundManager.setPaused(false);
            pause.setText("暂停");overlay.setVisible(false);
        };
        overlay.setContinueAction(resume);
        pause.addActionListener(e->{
            if(game.isPaused())resume.run();
            else{
                game.setPaused(true);SoundManager.setPaused(true);
                pause.setText("继续");overlay.showPause();
            }
        });
        restart.addActionListener(e->startGame());
        menu.addActionListener(e->showMenu());
        game.setOnFinished(()->{
            pause.setEnabled(false);pause.setText("本局结束");
            overlay.showResult(game.isWon());
        });
        JLayeredPane stage=new JLayeredPane(){
            @Override public void doLayout(){
                game.setBounds(0,0,getWidth(),getHeight());
                overlay.setBounds(0,0,getWidth(),getHeight());
            }
        };
        stage.setPreferredSize(new Dimension(GameModel.WIDTH,GameModel.HEIGHT));
        stage.add(game,JLayeredPane.DEFAULT_LAYER);
        stage.add(overlay,JLayeredPane.PALETTE_LAYER);
        top.add(navigation,BorderLayout.NORTH);
        top.add(game.getToolbar(),BorderLayout.CENTER);
        add(top,BorderLayout.NORTH);
        add(stage,BorderLayout.CENTER);
        add(game.getStatusBar(),BorderLayout.SOUTH);
        revalidate();repaint();
    }
}