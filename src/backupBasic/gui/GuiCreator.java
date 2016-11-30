package backupBasic.gui;
/*(c)2016, Tobias Hotz
 * Further Information can be found in Info.txt
 */

import backupBasic.util.ThreadedBackup;
import backupBasic.util.i18n;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

/**
 * @author Tobias Hotz
 */
public class GuiCreator extends JFrame implements ActionListener {
	private static final Logger logger = i18n.getLogger(GuiCreator.class);
	
	private static final long serialVersionUID = -622181498516220508L;
	
	private final static Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	
	public static String OutDir;
	public static String SourceDir;
	//Für Cancel-Button
	private static boolean BackupInProgress = false;

	private JPanel MainContent, ProgressContent;
	private JButton Ok, Cancel, Source, Dest, CancelP;
	private JLabel SourceTitle, DestTitle, SourcePath, DestPath;
	private JLabel ProgressText;
	private JCheckBox CheckOnFinish, CheckOldDir;
	public static JProgressBar progress;
	
    public GuiCreator() {
    	createPanel();
        addPanel();
    }
    
    /**
     * Erstellt die Elemente für die Pannels
     */
    private void createPanel() {
		ActionListenerCheckBox checkBoxListener = new ActionListenerCheckBox();    	
		
		MainContent = new JPanel();
    	ProgressContent = new JPanel();
		Ok = new JButton("OK");
		Ok.setActionCommand("Ok");
		Ok.setToolTipText(i18n.translate("OKToolTip"));
        
		Cancel = new JButton("Cancel");
		Cancel.setActionCommand("Cancel");
		Cancel.setToolTipText(i18n.translate("CancelToolTip"));
		
		CancelP = new JButton("Cancel");
		CancelP.setActionCommand("Cancel");
		CancelP.setToolTipText(i18n.translate("CancelToolTip"));
		
		Source = new JButton(i18n.translate("ChangeSource"));
		Source.setActionCommand("Source");
		
		Dest = new JButton(i18n.translate("ChangeDest"));
		Dest.setActionCommand("Dest");

		SourceTitle = new JLabel("  " + i18n.translate("SourcePath") + "  ");
		DestTitle = new JLabel("  "+ i18n.translate("DestPath") + "  ");
		SourcePath = new JLabel("  " + SourceDir + "  ");
		DestPath = new JLabel("  " + OutDir + "  ");
		
		CheckOnFinish = new JCheckBox(i18n.translate("CheckDirCopyButton"));
		CheckOnFinish.setToolTipText(i18n.translate("CheckDirCopy"));
		CheckOnFinish.setSelected(true);
		CheckOnFinish.setActionCommand("ListCopiedDir");
		
		CheckOldDir = new JCheckBox(i18n.translate("CheckOldDirButton"));
		CheckOldDir.setToolTipText(i18n.translate("CheckOldDirToolTip"));
		CheckOldDir.setSelected(true);
		CheckOldDir.setActionCommand("ListOldDir");

		progress = new JProgressBar();
		progress.setMinimum(0);
		progress.setMaximum(4);
		ProgressText = new JLabel("Arbeitet... Dieser Vorgang kann einige Zeit dauern");
		
		SourceTitle.setHorizontalAlignment(SwingConstants.CENTER);
		DestTitle.setHorizontalAlignment(SwingConstants.CENTER);
		SourcePath.setHorizontalAlignment(SwingConstants.CENTER);
		DestPath.setHorizontalAlignment(SwingConstants.CENTER);
		CheckOnFinish.setHorizontalAlignment(SwingConstants.CENTER);
		CheckOldDir.setHorizontalAlignment(SwingConstants.CENTER);
		
		Ok.addActionListener(this);
		Cancel.addActionListener(this);
		CancelP.addActionListener(this);
		Source.addActionListener(this);
		Dest.addActionListener(this);
		//Checkboxen erhalten eigene Klasse zwecks Übersicht
		CheckOnFinish.addActionListener(checkBoxListener);
		CheckOldDir.addActionListener(checkBoxListener);
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
		MainContent.add(CheckOnFinish);
		MainContent.add(CheckOldDir);
		ProgressContent.add(ProgressText);
		ProgressContent.add(progress);
		ProgressContent.add(CancelP);
        add(MainContent);
    }
    
    
    
    /**
     * Erstellet eine GUI zum Auswählen von Quell- und Zielverzeichnis
     * @param DefaultOutDir
     * @param DefaultSourceDir
     */
	public static void createGui(String DefaultOutDir, String DefaultSourceDir) {
		OutDir = DefaultOutDir;
		SourceDir = (DefaultSourceDir);
		logger.fine("Lade GUI");
		
		GuiCreator MyFrame = new GuiCreator();
		MyFrame.setResizable(false);
		MyFrame.setTitle("BackupBasic");
		MyFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		MyFrame.pack();
		//Mittig Positionieren
		MyFrame.setLocation(dim.width/2-MyFrame.getSize().width/2, dim.height/2-MyFrame.getSize().height/2);
		MyFrame.setVisible(true);
	}
	
	/**
	 * Öffnet einen Dialog, um einen Ordner auszuwählen und gibt den neuen Pfad zurück
	 * @param Title
	 * @param label
	 * @param Dir
	 * @return Dir
	 */
	private String chooseDir(@Nullable String Title, @NotNull JLabel label, @NotNull String Dir) {
		JFileChooser DirChooser = new JFileChooser();
		if(label !=null)
			DirChooser.setDialogTitle(Title);
		DirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		//TODO Change to last dir?
		if(DirChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			Dir = DirChooser.getSelectedFile().toString() + "/";
			label.setText(Dir);
		}
		return Dir;
	}	
	
	/**
	 * Verwaltet die GUI-Events, außer CheckBox
	 */
	@Override
	public void actionPerformed(ActionEvent ActionEvent) {
		
		String Event = ActionEvent.getActionCommand();
		Window window = SwingUtilities.windowForComponent(Cancel);
		switch(Event) {
		case "Source":
			SourceDir = chooseDir(i18n.translate("ChooseSourceDir"), SourcePath , SourceDir);
			window.pack();
			window.setLocation(dim.width/2-window.getSize().width/2, dim.height/2-window.getSize().height/2);
			break;
		case "Dest":
			OutDir = chooseDir(i18n.translate("ChooseOutDir"), DestPath , OutDir);
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
            if (!BackupInProgress) {
                logger.info(i18n.translate("UserExit"));
                System.exit(0);
            } else {
                if(JOptionPane.showConfirmDialog(null, i18n.translate("WarinigOnClose")) == 0)
                    System.exit(0);
            }
            break;
		}
	}

}