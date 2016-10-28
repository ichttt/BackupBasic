package backupBasic.gui;
/*(c)2016, Tobias Hotz
 * Further Information can be found in Info.txt
 */
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import backupBasic.util.ThreadedBackup;

/**
 * @author Tobias Hotz
 */
public class GuiCreator extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler {
	private static final Logger logger = Logger.getLogger(GuiCreator.class.getName());
	static {
		logger.setLevel(Level.ALL);
	}
	
	private static final long serialVersionUID = -622181498516220508L;
	
	private final static Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	
	public static String OutDir;
	public static String SourceDir;
	//F�r Cancel-Button
	private static boolean BackupInProgress = false;

	private JPanel MainContent, ProgressContent, ProgressCancel;
	private JButton Ok, Cancel, Source, Dest, CancelP;
	private JLabel SourceTitle, DestTitle, SourcePath, DestPath;
	private JLabel ProgressText, ProgressTextCancel;
	private JCheckBox CheckSumOnFinish, CheckSumOldDir;
	
    public GuiCreator() {
    	createPanel();
        addPanel();
    }
    
    /**
     * Erstellt die Elemente f�r die Pannels
     */
    private void createPanel() {
		ActionListenerCheckBox checkBoxListener = new ActionListenerCheckBox();    	
		
		MainContent = new JPanel();
    	ProgressContent = new JPanel();
    	ProgressCancel = new JPanel();
		Ok = new JButton("OK");
		Ok.setActionCommand("Ok");
		Ok.setToolTipText("Beginnt das Backup.");
        
		Cancel = new JButton("Cancel");
		Cancel.setActionCommand("Cancel");
		Cancel.setToolTipText("Beendet das Programm.");
		
		CancelP = new JButton("Cancel");
		CancelP.setActionCommand("Cancel");
		CancelP.setToolTipText("Beendet das Programm.");
		
		Source = new JButton("Quellordner ausw�hlen...");
		Source.setActionCommand("Source");
		
		Dest = new JButton("Zielordner ausw�hlen...");
		Dest.setActionCommand("Dest");
		
		SourceTitle = new JLabel("  Der aktuelle Pfad zum Quellverzeichnis ist:  ");
		DestTitle = new JLabel("  Der aktuelle Pfad zum Zielverzeichnis ist:  ");
		SourcePath = new JLabel("  " + SourceDir + "  ");
		DestPath = new JLabel("  " + OutDir + "  ");
		
		CheckSumOnFinish = new JCheckBox("Berechne Pr�fsumme �ber kopierte Dateien");
		CheckSumOnFinish.setToolTipText("Berechnet die Pr�fsumme f�r die kopierten Dateien, um sicherzustellen, dass alle Dateien richtig kopiert worden sind. "
				+ "Bei schwachen CPUs oder gro�en Dateien kann dieser Vorgang l�nger dauern.");

		CheckSumOnFinish.setSelected(true);
		CheckSumOnFinish.setActionCommand("CheckSumOnFinish");
		
		CheckSumOldDir = new JCheckBox("Berechne Pr�fsumme �ber altes Backups");
		CheckSumOldDir.setToolTipText("Berechnet die Pr�fsumme f�r altes Backup(falls vorhanden), um zu �berpr�fen, ob ein Backup �berhaupt notwendig ist. "
				+ "Bei schwachen CPUs oder gro�en Dateien kann dieser Vorgang l�nger dauern.");
		CheckSumOldDir.setSelected(true);
		CheckSumOldDir.setActionCommand("CheckSumOldDir");
		
		ProgressText = new JLabel("Arbeitet... Dieser Vorgang kann einige Zeit dauern");
		ProgressTextCancel = new JLabel("Breche ab...");
		
		SourceTitle.setHorizontalAlignment(SwingConstants.CENTER);
		DestTitle.setHorizontalAlignment(SwingConstants.CENTER);
		SourcePath.setHorizontalAlignment(SwingConstants.CENTER);
		DestPath.setHorizontalAlignment(SwingConstants.CENTER);
		CheckSumOnFinish.setHorizontalAlignment(SwingConstants.CENTER);
		CheckSumOldDir.setHorizontalAlignment(SwingConstants.CENTER);
		
		Ok.addActionListener(this);
		Cancel.addActionListener(this);
		CancelP.addActionListener(this);
		Source.addActionListener(this);
		Dest.addActionListener(this);
		//Checkboxen erhalten eigene Klasse zwecks �bersicht
		CheckSumOnFinish.addActionListener(checkBoxListener);
		CheckSumOldDir.addActionListener(checkBoxListener);
        }
    
    /**
     * Mapped die Elemente zu den Pannels
     */
    private void addPanel() {
		MainContent.setLayout(new GridLayout(5, 2));
    	MainContent.add(SourceTitle);
		MainContent.add(DestTitle);
		MainContent.add(SourcePath);
		MainContent.add(DestPath);
		MainContent.add(Source);
		MainContent.add(Dest);
		MainContent.add(Ok);
		MainContent.add(Cancel);
		MainContent.add(CheckSumOnFinish);
		MainContent.add(CheckSumOldDir);
		ProgressContent.add(ProgressText);
		ProgressContent.add(CancelP);
		ProgressCancel.add(ProgressTextCancel);
        add(MainContent);
    }
    
    
    
    /**
     * Erstellet eine GUI zum Ausw�hlen von Quell- und Zielverzeichnis
     * @param OutDir
     * @param SourceDir
     */
	public static void createGui(String DefaultOutDir, String DefaultSourceDir) {
		OutDir = DefaultOutDir;
		SourceDir = (DefaultSourceDir);
		logger.fine("Lade GUI");
		
		GuiCreator MyFrame = new GuiCreator();
		MyFrame.setResizable(false);
		MyFrame.setTitle("BackupBasic");
		MyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		MyFrame.pack();
		//Mittig Positionieren
		MyFrame.setLocation(dim.width/2-MyFrame.getSize().width/2, dim.height/2-MyFrame.getSize().height/2);
		MyFrame.setVisible(true);
	}
	
	/**
	 * �ffnet einen Dialog, um einen Ordner auszuw�hlen und gibt den neuen Pfad zur�ck
	 * @param Title
	 * @param label
	 * @param Dir
	 * @return Dir
	 */
	private String chooseDir(String Title, JLabel label, String Dir) {
		JFileChooser DirChooser = new JFileChooser();
		DirChooser.setDialogTitle(Title);
		DirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		//TODO Change to last dir?
		if(DirChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			Dir = DirChooser.getSelectedFile().toString() + "/";
			logger.finer("Neuer " + label + " ist " + Dir);
			label.setText(Dir);
		}
		else {
			logger.finer("Kein neuer "+ label +" wurde festgelegt.");
		}
		return Dir;
	}	
	
	/**
	 * Verwaltet die GUI-Events, au�er CheckBox
	 */
	@Override
	public void actionPerformed(ActionEvent ActionEvent) {
		Thread.setDefaultUncaughtExceptionHandler(new GuiCreator());
		
		String Event = ActionEvent.getActionCommand();
		Window window = SwingUtilities.windowForComponent(Cancel);
		switch(Event) {
		case "Source":
			SourceDir = chooseDir("Bitte w�hlen Sie das Quellverzeichnis aus!", SourcePath , SourceDir);
			window.pack();
			window.setLocation(dim.width/2-window.getSize().width/2, dim.height/2-window.getSize().height/2);
			break;
		case "Dest":
			OutDir = chooseDir("Bitte w�hlen Sie das Zielverzeichnis aus!", DestPath , OutDir);
			window.pack();
			window.setLocation(dim.width/2-window.getSize().width/2, dim.height/2-window.getSize().height/2);
			break;
		case "Ok":
			//Arbeitet-Dialog
			getContentPane().removeAll();
			getContentPane().add(ProgressContent);
			window.repaint();
			printAll(getGraphics());
			window.pack();
			window.setLocation(dim.width/2-window.getSize().width/2, dim.height/2-window.getSize().height/2);
			BackupInProgress = true;
			ThreadedBackup.startThreadedBackup();
			break;
		case "Cancel":
			if(BackupInProgress == false) {
				logger.info("Beende Programm durch Benutzer");
				getContentPane().removeAll();
				getContentPane().add(ProgressCancel);
				window.repaint();
				printAll(getGraphics());
				System.exit(0);
			}
			else if(JOptionPane.showConfirmDialog(null, "Es wurden m�glicherweise noch nicht alle Dateien kopiert. Fortfahren?") == 0) {
				//Das Funktioniert noch nicht, da System.exit den Thread direkt killt
				//TODO In Shutdown-Hook verschieben
				getContentPane().removeAll();
				getContentPane().add(ProgressCancel);
				window.repaint();
				printAll(getGraphics());
				System.exit(0);
			}
			break;
		}
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		e.printStackTrace();
		JOptionPane.showMessageDialog(null, "Unbehandelte Ausnahme beim Verarbeiten des Knopfdruckes. Das Programm funktioniert m�glicherweise nicht wie erwartet.\nFehler:"
									  + e + "\nWeitere Informationen in der Console", "ERROR" , JOptionPane.ERROR_MESSAGE);
	}

}