import javax.swing.JFileChooser;
import javax.swing.JFrame;









public class Start extends JFrame
{
	
	private static Start start ;
	private String fromPath = null;
	private String toPath = null;
	public static void main(String[] args)
	{
		start = new Start();
//		LoadFile lodFile = new LoadFile("F:/360云盘/云同步/小立方/英文资料", "F:/360云盘/云同步/小立方/temp");
//
//		ArrangeFile arrangeFile = new ArrangeFile("F:/360云盘/云同步/小立方/temp","F:/360云盘/云同步/小立方/merge");
//		arrangeFile.merge("译林牛津小学英语六年级下册");
		
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
    	int i = fc.showDialog(start, "选择文件源目录");
    	if(i == JFileChooser.APPROVE_OPTION) {
			String fromPath = fc.getSelectedFile().getPath();
			System.out.println(fromPath);
    	}
    	
    	
		JFileChooser fc1 = new JFileChooser();
		fc1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
    	int i1 = fc.showDialog(start, "选择文件目的地目录");
    	if(i1 == JFileChooser.APPROVE_OPTION) {
			String toPath = fc.getSelectedFile().getPath();
			System.out.println(toPath);
    	}
		
	}
}