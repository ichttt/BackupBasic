package backupBasic.gui;
/*(c)2016, Tobias Hotz
 * Further Information can be found in Info.txt
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import backupBasic.backup.CopyManager;

/**
 * @author Tobias Hotz
 */
public class ActionListenerCheckBox implements ActionListener {
	
	//TODO Nötig?
	@Override
	public void actionPerformed(ActionEvent ActionEvent) {
		Thread.setDefaultUncaughtExceptionHandler(new GuiCreator());
		String Event = ActionEvent.getActionCommand();
		switch(Event) {
		case "CheckSumOnFinish":
			CopyManager.calcCheckSumOnFinish = !CopyManager.calcCheckSumOnFinish;
			break;
		case "CheckSumOldDir":
			CopyManager.calcCheckSumOldDir = !CopyManager.calcCheckSumOldDir;
			break;
		}
	}
}