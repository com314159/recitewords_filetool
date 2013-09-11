import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.HashMap;


public class LoadFile
{
	private String fromPath;
	private String toPath;
	
	private HashMap<String, String> englishMap = new HashMap<String,String>();
	private HashMap<String, String> chineseMap = new HashMap<String,String>();
	private	File fromFile ;
	private	File enMp3Directory,
				 chMp3Directory;
	private	File fromTxt;
	private File toEnMp3,
				 toChMp3,
				 toIndex;
	private File toTxt;
	private RandomAccessFile readMp3 = null,
							 writeTxt = null,
							 writeEnMp3 = null,
							 writeChMp3 = null,
							 writeIndex = null;
	private BufferedReader readTxt;
	private int[] unitPosition; 
	public LoadFile(String from,String to)
	{
		fromPath = from;
		toPath = to;
		init();
		onStart();
		close();
	}
	private void init()
	{
		fromFile = new File(fromPath);
		assert(fromFile.exists()&&fromFile.isDirectory());
	}
	private void onStart()
	{
		File[] allBook = fromFile.listFiles();
		for(int i=0;i<allBook.length;++i)
		{
			toEnMp3 = new File(toPath + "/" + allBook[i].getName() + ".em");
			toChMp3 = new File(toPath + "/" + allBook[i].getName() + ".cm");
			toTxt = new File(toPath + "/" + allBook[i].getName() + ".t");
			toIndex =  new File(toPath + "/" + allBook[i].getName() + ".id");
			try
			{
				if(toEnMp3.exists())
				{
					toChMp3.delete();
					toEnMp3.delete();
					toTxt.delete();
					toIndex.delete();
				}
				toEnMp3.createNewFile();
				toChMp3.createNewFile();
				toTxt.createNewFile();
				toIndex.createNewFile();
				if(writeTxt != null) 
				{
					writeTxt.close();
					writeEnMp3.close();
					writeChMp3.close();
					writeIndex.close();
				}
				writeTxt = new RandomAccessFile(toTxt, "rw");
				writeEnMp3 = new RandomAccessFile(toEnMp3,"rw");
				writeChMp3 = new RandomAccessFile(toChMp3,"rw");
				writeIndex = new RandomAccessFile(toIndex, "rw");
				
				File[] allUnit = allBook[i].listFiles();
				unitPosition = new int[allUnit.length];
				int startPos = 14 + unitPosition.length * 4;
				writeTxt.seek((long)startPos);
				for(int j=0;j<allUnit.length;++j)
				{	
					File[] temp = allUnit[j].listFiles();
					assert(temp.length == 3);
					enMp3Directory = findFile(temp, "英文");
					chMp3Directory = findFile(temp, "中文");
					fromTxt = findFile(temp, "word.txt");
					moveMp3();
					int pos = (int)writeTxt.getFilePointer();
					unitPosition[j] = pos;
					String s = tidyUnitName(allUnit[j].getName());
					writeTxt.write(s.getBytes("UTF-8"));
					writeTxt.write(13);
					writeTxt.write(10);
					
					writeIndex.write(s.getBytes("UTF-8"));
					writeIndex.write(13);
					writeIndex.write(10);					
					
					moveTxt();
				}
				endUnit();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}		
	}
	
	private void moveMp3()
	{
		File[] mp3List = enMp3Directory.listFiles();
		for(int i=0;i<mp3List.length;++i)
		{
			try
			{
			 if(readMp3 != null)
				 readMp3.close();
			 readMp3 = new RandomAccessFile(mp3List[i], "r");
			 byte[] b = new byte[8192];
			 int len ,
			 start = (int)(writeEnMp3.getFilePointer()),
			 fileLen = 0;
			 while ( (len = readMp3.read(b)) != -1)
			 {	
				fileLen += len;
				writeEnMp3.write(b, 0, len);
			 }
			 String s = String.valueOf(start),
					 l = String.valueOf(fileLen);
			 String s1 = removeSuffix(mp3List[i].getName());
			 s1 = tidyWordName(s1);
			 englishMap.put(s1,s+" "+l);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		mp3List = chMp3Directory.listFiles();
		for(int i=0;i<mp3List.length;++i)
		{
			try
			{
		     if(readMp3 != null)
					readMp3.close();
			 readMp3 = new RandomAccessFile(mp3List[i], "r");
			 byte[] b = new byte[8192];
			 int len ,
			 start = (int)(writeChMp3.getFilePointer()),
			 fileLen = 0;
			 while ( (len = readMp3.read(b)) != -1)
			 {	
				fileLen += len;
				writeChMp3.write(b, 0, len);
			 }
			 String s = String.valueOf(start),
					 l = String.valueOf(fileLen);
			 String s1 = removeSuffix(mp3List[i].getName());
			 s1 = tidyWordName(s1);
			 chineseMap.put(s1,s+" "+l);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void moveTxt()
	{
		try
		{
			InputStreamReader in = new InputStreamReader(new FileInputStream(fromTxt),"UTF-8");
			if(readTxt != null)
				readTxt.close();
			readTxt = new BufferedReader(in);
			String origString = null;
			int state = 1;
			String wordName = null;
			boolean eof = false;
			while((origString = readTxt.readLine())!= null)
			{
				switch(state)
				{
				case 1:
					if(origString.length() == 0)
					{
						eof = true;
						break;
					}
					writeTxt.write(origString.getBytes("UTF-8"));
					wordName = tidyWordName(origString);
					break;
				case 2:
					if(origString.length() == 0)
					{
						origString = "#"; //表示发音为空
					}
					writeTxt.write(origString.getBytes("UTF-8"));				
					break;
				case 3:
					if(origString.length() == 0)
					{
						System.out.println("Error when state 3");
						assert false;
					}
					writeTxt.write(origString.getBytes("UTF-8"));
					break;
				case 4:
					if(origString.length() != 0)
					{
						System.out.println("Error when state 4");
						assert false;
					}
					String enPos = englishMap.get(wordName);
					if(enPos == null)
					{
						System.out.println(wordName);
						assert false;
					}
					englishMap.remove(wordName);
					String chPos = chineseMap.get(wordName);
					if(chPos == null)
					{
						System.out.println(wordName);
						assert false;
					}
					chineseMap.remove(wordName);
					String pos = enPos + " " + chPos;
					writeTxt.write(pos.getBytes("UTF-8"));
					break;
				}
				if(!eof)
				{
				writeTxt.write(13);
				writeTxt.write(10);
				state = (state)%4+1;
				}
			}
			
			if(state == 3)
			{
				String enPos = englishMap.get(wordName);
				if(enPos == null)
				{
					assert false;
				}
				englishMap.remove(wordName);
				String chPos = chineseMap.get(wordName);
				if(chPos == null)
				{
					assert false;
				}
				chineseMap.remove(wordName);
				String pos = enPos + " " + chPos;
				writeTxt.write(pos.getBytes("UTF-8"));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}

	private void endUnit ()
	{
		try
		{
		writeTxt.seek(10);
		writeTxt.writeInt(unitPosition.length*4);
		for(int i=0;i<unitPosition.length;++i)
		{
			writeTxt.writeInt(unitPosition[i]);
		}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void close()
	{
		try
		{
		writeTxt.close();
		writeEnMp3.close();
		writeChMp3.close();
		readMp3.close();
		readTxt.close();
        writeIndex.close();
		System.out.println("load successfuled");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private File findFile(File[] filelist,String name)
	{
		for(int i=0;i<filelist.length;++i)
		{
			if(filelist[i].getName().equals(name))
				return filelist[i];
		}
		assert false;
		return null;
	}
	
	private String tidyUnitName(String s)
	{
		String [] t = s.split("-");
		assert t.length == 2;
		return t[1];
	}
	
	private String tidyWordName(String s)
	{
		String word = "";
		int start=0,end = s.length() - 1;
		for(int i=0;i<s.length();++i)
		{
			if(isWord(s.charAt(i)))
			{
				start = i;
				break;
			}
		}
		for(int i=s.length()-1;i>=0;--i)
		{
			if(isWord(s.charAt(i)))
			{
				end = i;
				break;
			}
		}
		for(int i=start;i<=end;++i)
		{
			word = word + s.charAt(i);
		}
		assert word.length()!=0;
		return word;
	}
	
	private String tidySpace(String s)
	{
		String name = "";
		int start=0,end = s.length() - 1;
		for(int i=0;i<s.length();++i)
		{
			if(s.charAt(i) != ' ')
			{
				start = i;
				break;
			}
		}
		for(int i=s.length()-1;i>=0;--i)
		{
			if(s.charAt(i) != ' ')
			{
				end = i;
				break;
			}
		}
		for(int i=start;i<=end;++i)
		{
			name = name + s.charAt(i);
		}
		return name;
	}
	
	private String removeSuffix(String fileName)
	{
		String s = "";
		int pos = -1;
		for(int i=fileName.length()-1;i>=0;--i)
		{
			if(fileName.charAt(i) == '.') 
			{
				pos = i-1;
				break;
			}
		}
		assert pos!=-1;
		for(int i=pos;i>=0;--i)
		{
			s = fileName.charAt(i) + s;
		}
		assert s.length()!=0;
		return s;
	}
	
	private boolean isWord(char c)
	{
		int a = c;
		if(a == 65279) return false;
		if( c>= 'A' && c<='z' || c>='a' && c <='z' || c == '\'') 
			return true;
				return false;
	}
}
