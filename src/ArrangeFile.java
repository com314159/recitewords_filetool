import java.io.File;
import java.io.RandomAccessFile;


public class ArrangeFile
{
	String fromPath;
	String toPath;
	File fromDirectory,toDirectory;
	public ArrangeFile(String from,String to)
	{
		fromPath = from;
		toPath = to;
		fromDirectory = new File(from);
		toDirectory = new File(to);
	}
	
	public void merge(String bookName)
	{
		RandomAccessFile read,write;
		File toFile = new File(toPath + "/" + bookName + ".gzc");
		File[] files = fromDirectory.listFiles();
		File enFile = Tool.findFile(files,bookName + ".em");
		File chFile = Tool.findFile(files, bookName + ".cm");
		File Txt = Tool.findFile(files, bookName + ".t");
		File idFile = Tool.findFile(files, bookName + ".id");
		try
		{
			write = new RandomAccessFile(toFile, "rw");
			write.writeInt((int)enFile.length());
			write.writeInt((int)chFile.length());
			write.writeInt((int)Txt.length());
			
			read = new RandomAccessFile(enFile, "rw");
			writeInto(read, write);
			read.close();
			
			read = new RandomAccessFile(chFile, "rw");
			writeInto(read, write);
			read.close();
			
			read = new RandomAccessFile(Txt, "rw");
			writeInto(read, write);
			read.close();
			
			read = new RandomAccessFile(idFile, "rw");
			writeInto(read, write);
			
			read.close();
			write.close();
			System.out.println("merge successful");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void split(String bookName)
	{
		try
		{
			RandomAccessFile read;
			RandomAccessFile enMp3,
							 chMp3,
							 txt,
							 idRaf;
			File readFile = new File(toPath+ "/"+bookName+".gzc");
			File enFile = new File(fromPath+ "/new/" + bookName + ".em"), 
				 chFile = new File(fromPath+ "/new/" + bookName + ".cm"),
				 txtFile = new File(fromPath+ "/new/" + bookName + ".t"),
				 idFile = new File(fromPath+ "/new/" + bookName + ".id");
			read = new RandomAccessFile(readFile, "r");
			enMp3 = new RandomAccessFile(enFile, "rw");
			chMp3 = new RandomAccessFile(chFile, "rw");
			txt = new RandomAccessFile(txtFile, "rw");
			idRaf = new RandomAccessFile(idFile, "rw");
			byte[] b = new byte[12];
			read.read(b);
			int[] pos = Tool.byteArrayToint(b);
			writeInto(read, enMp3,pos[0]);
			writeInto(read, chMp3,pos[1]);
			writeInto(read, txt,pos[2]);
			writeInto(read,idRaf,(int)(readFile.length()-read.getFilePointer()) );
			read.close();
			enMp3.close();
			chMp3.close();
			txt.close();
			System.out.println("split succeed");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	private void writeInto(RandomAccessFile from,RandomAccessFile to,int l)
	{
		try
		{
			byte[] b = new byte[8192];
			int len;
			while(l>0)
			{
				if(l>8192)
				{
					len = from.read(b);
					l-=len;
					to.write(b,0,len);
				}
				else
				{
					from.read(b, 0, l);
					to.write(b, 0, l);
					l = 0;
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	private void writeInto(RandomAccessFile from,RandomAccessFile to)
	{
		try
		{
			byte[] b = new byte[8192];
			int len;
			while((len = from.read(b))!= -1)
			{
				to.write(b, 0, len);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
