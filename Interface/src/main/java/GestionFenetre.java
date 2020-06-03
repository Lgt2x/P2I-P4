import javax.swing.JFrame;

public class GestionFenetre {

    public static void main(String[] args) throws Exception {

        System.out.println("lancement du programme...");
        JFrame monitoringFrame = MonitoringFrameForm.build();
        FenetreVisualisation maFenetre = new FenetreVisualisation();
    }
}
