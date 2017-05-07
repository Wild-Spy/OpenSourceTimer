package customwidgets;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {

    private BufferedImage image = null;

    public ImagePanel() {
    }

    public boolean setImage(String imagePath) {
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) return;
        g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this); // see javadoc for more info on the parameters
    }

}