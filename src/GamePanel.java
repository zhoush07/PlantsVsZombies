import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.EnumMap;

/** 只负责界面、缩放、鼠标输入和刷新；植物行为由各植物类完成。 */
public class GamePanel extends JPanel {
    private final GameModel game = new GameModel();
    private final BufferedImage background = Assets.load("graphics/Map/map0.jpg");
    private final JPanel toolbar = new JPanel(new BorderLayout());
    private final JLabel sunLabel = new JLabel(), progressLabel = new JLabel();
    private final JLabel statusLabel = new JLabel();
    private final JToggleButton shovelButton = new JToggleButton("铲子");
    private final EnumMap<PlantType, JToggleButton> cards = new EnumMap<>(PlantType.class);
    private final Timer timer;
    private boolean paused;
    private boolean closed;
    private boolean finishNotified;

    private Runnable onFinished = () -> {};

    public boolean isPaused() {
        return paused;
    }

    public void setOnFinished(Runnable action) {
        onFinished = action;
    }

    public void setPaused(boolean value) {
        if (closed || game.gameOver || paused == value) {
            return;
        }

        paused = value;
        timer.stop();

        // 清除暂停期间的时间，继续时不会突然补算
        lastTickNanos = System.nanoTime();
        pendingNanos = 0;

        if (!paused && isDisplayable()) {
            timer.start();
        }

        refreshInterface();
        repaint();
    }

    public void stopGame() {
        closed = true;
        timer.stop();
        pendingNanos = 0;
    }
    private long lastTickNanos;
    private long pendingNanos;

    // 根据现实经过的时间，补齐应该执行的游戏更新
    private void advanceTo(long now) {
        if (paused || closed) {
            return;
        }
        pendingNanos += Math.max(0, now - lastTickNanos);
        lastTickNanos = now;

        long stepNanos = GameModel.STEP * 1_000_000L;

        while (pendingNanos >= stepNanos) {
            game.update();
            pendingNanos -= stepNanos;

            if (game.gameOver
                    && game.blasts.isEmpty()
                    && game.effects.isEmpty()) {
                pendingNanos = 0;
                break;
            }
        }
    }

    public GamePanel() {
        setPreferredSize(new Dimension(GameModel.WIDTH, GameModel.HEIGHT));
        setBackground(new Color(223, 234, 210));
        buildInterface();
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent event) {
                if (paused || closed) {
                    return;
                }
                double scale = viewScale();
                if (scale <= 0) return;
                double x = (event.getX() - (getWidth() - GameModel.WIDTH * scale) / 2) / scale;
                double y = (event.getY() - (getHeight() - GameModel.HEIGHT * scale) / 2) / scale;
                if (x < 0 || y < 0 || x >= GameModel.WIDTH || y >= GameModel.HEIGHT) return;
                game.handleClick((int)x, (int)y);
                refreshInterface();
                repaint();
            }
        });
        timer = new Timer(GameModel.STEP, e -> {
            if (paused || closed) {
                return;
            }

            advanceTo(System.nanoTime());

            // 只通知一次本局结束
            if (game.gameOver && !finishNotified) {
                finishNotified = true;
                onFinished.run();
            }

            refreshInterface();
            repaint();

            // 等死亡和爆炸效果播完，再停止刷新
            if (game.gameOver
                    && game.blasts.isEmpty()
                    && game.effects.isEmpty()) {
                ((Timer) e.getSource()).stop();
            }
        });
        refreshInterface();
    }

    public JPanel getToolbar() { return toolbar; }
    public JLabel getStatusBar() { return statusLabel; }

    private void buildInterface() {
        Color cream = new Color(243, 238, 220);
        toolbar.setBackground(cream);
        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        info.setOpaque(false);
        info.add(sunLabel);
        info.add(progressLabel);
        JPanel buttons = new JPanel(new GridLayout(1, PlantType.values().length + 1, 5, 0));
        buttons.setOpaque(false);
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        // 自动从登记表生成卡片；新增植物不用在这里重复写按钮代码。
        for (PlantType type : PlantType.values()) {
            JToggleButton button = new JToggleButton();
            cards.put(type, button);
            buttons.add(button);
            button.addActionListener(e -> {
                if (paused || closed) {
                    return;
                }

                game.selectPlant(type);
                refreshInterface();
                repaint();
            });
        }
        buttons.add(shovelButton);
        shovelButton.addActionListener(e -> {
            if (paused || closed) {
                return;
            }

            game.selectShovel();
            refreshInterface();
            repaint();
        });
        for (Component component : buttons.getComponents()) {
            component.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
            ((AbstractButton)component).setMargin(new Insets(8, 2, 8, 2));
        }
        toolbar.add(info, BorderLayout.NORTH);
        toolbar.add(buttons, BorderLayout.CENTER);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(cream);
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }

    private void refreshInterface() {
        sunLabel.setText("阳光：" + game.sunshine);
        progressLabel.setText("出场 " + game.spawnedCount + "/" + GameModel.TOTAL_ZOMBIES
                + "  消灭 " + game.defeatedCount);
        for (PlantType type : PlantType.values()) {
            JToggleButton button = cards.get(type);
            int remaining = game.cooldowns[type.ordinal()];
            String cardText =
                    "<html><center>" + type.displayName + " " + type.cost + "<br>"
                            + (remaining > 0
                            ? ((remaining + 999) / 1000) + "秒后可用"
                            : "可选择")
                            + "</center></html>";

            if (!cardText.equals(button.getText())) {
                button.setText(cardText);
            }
            button.setSelected(game.selectedPlant == type);
            button.setEnabled(!game.gameOver && !paused && !closed);
            button.setForeground(remaining > 0 || game.sunshine < type.cost
                    ? Color.GRAY : new Color(30, 90, 30));
        }
        shovelButton.setSelected(game.shovelSelected);
        shovelButton.setEnabled(!game.gameOver && !paused && !closed);
        statusLabel.setText(
                (paused ? "已暂停，点击继续恢复游戏" : game.tip)
                        + "    |    F11 全屏 / Esc 退出全屏");
    }

    private double viewScale() {
        return Math.min(getWidth() / (double)GameModel.WIDTH, getHeight() / (double)GameModel.HEIGHT);
    }
    @Override
    public void addNotify() {
        super.addNotify();

        lastTickNanos = System.nanoTime();
        pendingNanos = 0;

        if (!paused && !closed
                && (!game.gameOver
                || !game.blasts.isEmpty()
                || !game.effects.isEmpty())) {
            timer.start();
        }
    }
    @Override public void removeNotify() { timer.stop(); super.removeNotify(); }

    @Override protected void paintComponent(Graphics original) {
        super.paintComponent(original);
        Graphics2D g = (Graphics2D)original.create();
        try {
            double scale = viewScale();
            if (scale <= 0) return;
            g.translate((getWidth() - GameModel.WIDTH * scale) / 2,
                    (getHeight() - GameModel.HEIGHT * scale) / 2);
            g.scale(scale, scale);
            g.clipRect(0, 0, GameModel.WIDTH, GameModel.HEIGHT);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(background, 0, 0, GameModel.WIDTH, GameModel.HEIGHT, this);
            for (int row = 0; row < GameModel.ROWS; row++) {
                for (int col = 0; col < GameModel.COLS; col++) {
                    Plant plant = game.plants[row][col];
                    boolean planting = game.selectedPlant != null && plant == null;
                    boolean digging = game.shovelSelected && plant != null;
                    if (planting || digging) {
                        g.setColor(planting ? new Color(255,255,100,55) : new Color(255,100,60,90));
                        g.fillRect(GameModel.GRID_X + col * GameModel.CELL_W,
                                GameModel.GRID_Y + row * GameModel.CELL_H,
                                GameModel.CELL_W, GameModel.CELL_H);
                    }
                    if (plant != null) plant.draw(g);
                }
            }
            for (Enemy enemy : game.enemies) enemy.zombie.draw(g);
            for (Shot shot : game.shots) shot.bullet.draw(g);
            for (Blast blast : game.blasts) blast.draw(g);
            for (Sun sun : game.suns) sun.draw(g);
        } finally { g.dispose(); }
    }
}
