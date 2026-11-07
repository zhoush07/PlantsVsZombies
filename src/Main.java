import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameWindow().setVisible(true));
    }

    private static class GameWindow extends JFrame {
        private boolean fullScreen;
        private Rectangle previousBounds;
        private int previousState;

        GameWindow() {
            super("植物大战僵尸");
            setContentPane(new GameController());
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setResizable(true);
            setMinimumSize(new Dimension(900, 500));
            pack();
            setLocationRelativeTo(null);
            setExtendedState(JFrame.MAXIMIZED_BOTH);

            bind("F11", "toggleFullScreen", () -> setFullScreen(!fullScreen));
            bind("ESCAPE", "exitFullScreen", () -> setFullScreen(false));
        }

        private void bind(String key, String name, Runnable action) {
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(key), name);
            getRootPane().getActionMap().put(name, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    action.run();
                }
            });
        }

        private void setFullScreen(boolean enabled) {
            if (enabled == fullScreen) return;
            if (enabled) {
                previousBounds = getBounds();
                previousState = getExtendedState();
                Rectangle screen = getGraphicsConfiguration().getBounds();
                dispose();
                setExtendedState(JFrame.NORMAL);
                setUndecorated(true);
                setBounds(screen);
            } else {
                dispose();
                setUndecorated(false);
                setExtendedState(JFrame.NORMAL);
                setBounds(previousBounds);
                setExtendedState(previousState);
            }
            fullScreen = enabled;
            setVisible(true);
        }
    }
}
