import java.io.File;
import java.io.RandomAccessFile;
import java.util.Vector;


public class ReadFile
{
	public Vector<Word> words ;
	public String unitName;
	private File fromFileDirectory = null;
	private RandomAccessFile rafChMp3 = null,
							 rafEnMp3 = null,
							 rafTxt = null;
	private String bookNameForEn = "",
				   bookNameForCh = "";
	private String tempPath;
	private RandomAccessFile rmp3;
	public ReadFile(String path,String tempPath)
	{
		this.tempPath = tempPath;
		fromFileDirectory = new File(path);
	}

	
	
	public void readFile(String bookName,int unitNumber)
	{
		words = new Vector<Word>();
		File[] fileBookList = fromFileDirectory.listFiles();
		File   fileTxt = findFile(fileBookList,bookName + ".t");
		assert fileTxt != null;
		try
		{
			if(rafTxt != null)
			{
				rafTxt.close();
			}
			rafTxt = new RandomAccessFile(fileTxt, "r");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		startRead(unitNumber);
		
	}
	
	private void startRead(int unitNumber)
	{
		try
		{
			rafTxt.seek(10);
			int len = rafTxt.readInt();
			byte[] b = new byte[len];
			rafTxt.read(b);
			int[] unitPosition = byteArrayToint(b);
			int start = unitPosition[unitNumber];
			int unitLen;
			if(unitNumber == unitPosition.length - 1) 
			{
				unitLen = (int)rafTxt.length() - start;
			}
			else 
			{
				unitLen = (int)unitPosition[unitNumber+1] - start;
			}
			byte[] buffer = new byte[unitLen];
			rafTxt.seek(start);
			rafTxt.read(buffer);
			String s = new String(buffer, "UTF-8");
			parser(s);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void parser(String s)
	{
		String temp[] = s.split("\r\n");
		unitName = temp[0];
		for(int i=1;i<temp.length - 1;i+=4)
		{
			Word word = new Word(temp[i],temp[i+1],temp[i+2],temp[i+3]);
			words.add(word);
		}
	}
	
	public void readMp3(String bookName,Word word,int i)
	{
		File[] fileBookList = fromFileDirectory.listFiles();
		File fileMp3;
		int start,len;
		switch (i)
		{
		case 1:
			start = word.enStart;
			len = word.enlen;
			if(bookName.equals(bookNameForEn))
			{
				createTempMp3(rafEnMp3, start, len, word.name + "en.mp3");
				return;
			}
			bookNameForEn = bookName;
			fileMp3 = findFile(fileBookList,bookName + ".em");
			assert fileMp3!= null;
			try
			{
				if(rafEnMp3 != null)
				{
					rafEnMp3.close();
				}
				rafEnMp3 = new RandomAccessFile(fileMp3, "rw");
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			createTempMp3(rafEnMp3, start, len, word.name + "en.mp3");
			break;
		case 2:
			start = word.chStart;
			len = word.chlen;
			if(bookName.equals(bookNameForCh))
			{
				createTempMp3(rafChMp3, start, len, word.name + "ch.mp3");
				return;
			}			
			fileMp3 = findFile(fileBookList, bookName + ".cm");
			bookNameForCh = bookName;
			assert fileMp3 != null;
			try
			{
				if(rafChMp3!=null)
				{
					rafChMp3.close();
				}
				rafChMp3 = new RandomAccessFile(fileMp3, "rw");
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			createTempMp3(rafChMp3, start, len, word.name + "ch.mp3");
			break;
		}
		
	}

	private void createTempMp3(RandomAccessFile raf,int s,int l,String fileName)
	{
		File mp3 = new File(tempPath +"/" + fileName);
		if(mp3.length()>0) 
		{
			mp3.delete();
			try
			{
				mp3.createNewFile();	
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		try
		{
			rmp3 = new RandomAccessFile(mp3,"rw");
			byte[] b;
			raf.seek(s);
			if(l<=8192)
			{
				b = new byte[l];
				raf.read(b);
				rmp3.write(b);
				rmp3.close();
				return;
			}
			else 
			b = new byte[8192];
			int len;
			while (l>0)
			{
				if(l>=8192)
				{
					len = raf.read(b);
					l -= len;
					rmp3.write(b);
				}
				else
				{
					raf.read(b,0,l);
					rmp3.write(b,0,l);
					l = 0;
				}
				
			}
			rmp3.close();
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	 protected void finalize()
	{
	  try
	  {
		if(rafChMp3 != null) 
			rafChMp3.close();
		if(rafEnMp3 != null) 
			rafEnMp3.close();
		if(rafTxt != null) 
			rafTxt.close();
	  }
	  catch (Exception e)
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
	private int byteToint(byte [] b)
	{
		int a = 0;
		int t;
		for(int i=0;i<4;++i)
		{
			t = (int)b[i];
			t = t&0xff;
		    a = (a<<8) + t;
		}
		return a;
	}
	private int[] byteArrayToint(byte[] b)
	{
		int[] a = new int[b.length/4];
		byte[] t = new byte[4];
		for(int i=0;i<a.length;++i)
		{
			for(int j=0;j<4;++j)
			{
				t[j] = b[j+i*4];
			}
			a[i] = byteToint(t);
		}
		return a;
	}
}
