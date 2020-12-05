import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

public class Compiler {
	LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer();
	RecursiveDescentParser parser = new RecursiveDescentParser();
	public static void main(String agrs[]){
		Compiler compiler = new Compiler();
		compiler.compile();
  }
	public void compile() {
		if(!getSourceCode()) 
			return;				
		lexicalAnalyzer.analyse();
		parser.parse();
		JOptionPane.showMessageDialog(null,"Compile finished!","Note",JOptionPane.PLAIN_MESSAGE);
		
	}
	private boolean getSourceCode() {
		String path = null;
		int result = 0;
		JFileChooser fileChooser = new JFileChooser();
		FileSystemView fsv = FileSystemView.getFileSystemView();  //get the path of the desktop
		fileChooser.setCurrentDirectory(fsv.getHomeDirectory());
		fileChooser.setDialogTitle("Select the pas file to be compiled");
		fileChooser.setApproveButtonText("ok");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		result = fileChooser.showOpenDialog(null);
		if(JFileChooser.APPROVE_OPTION == result) {
			path=fileChooser.getSelectedFile().getPath();
			File tmpFile = new File(path);
			String fileName,filePath;
			String fileHead,fileTail;
			if(!tmpFile.exists()) {
				JOptionPane.showMessageDialog(null,"Not a file!","Warning!",JOptionPane.PLAIN_MESSAGE);
			}
			else {				
				fileName = tmpFile.getName();
				filePath = tmpFile.getParent();
				String data[] = fileName.split("\\.");
				fileHead = data[0];
				fileTail = data[1];
				fileHead = filePath+"\\"+fileHead;
				if(fileTail.equals("pas")||fileTail.equals("txt")) {					
					lexicalAnalyzer.getPath(fileHead);
					parser.getPath(fileHead);	
					return true;
				}
				else 
					JOptionPane.showMessageDialog(null,"An illegal file!","Error!",JOptionPane.PLAIN_MESSAGE);		
			}
		}		
		return false;
	}
	
}
