package backupBasic.gui;
/*(c)2016, Tobias Hotz
 * Further Information can be found in Info.txt
 */

import backupBasic.backup.CopyManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Tobias Hotz
 */
public class ActionListenerCheckBox implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent ActionEvent) {
		String Event = ActionEvent.getActionCommand();
		switch(Event) {
		case "CheckSumOnFinish":
			CopyManager.checkFilesOnFinish = !CopyManager.checkFilesOnFinish;
			break;
		case "ListOldDir":
			CopyManager.checkFilesOldDir = !CopyManager.checkFilesOldDir;
			break;
		}
	}
}