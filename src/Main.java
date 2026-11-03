import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("植物大战僵尸");

            window.setContentPane(new GamePanel());
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.pack();
            window.setResizable(false);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}