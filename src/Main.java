import gui.LoginFrame;
import util.UIUtils;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        UIUtils.setupDarkUI();

        SwingUtilities.invokeLater(() -> {
            new LoginFrame();
        });

    }
}