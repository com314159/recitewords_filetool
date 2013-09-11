
public class Word
{
	public String  name = null;
	public String  explain = null;
	public String  soundmark = null;
	public int 	   enStart = -1,
				   enlen = 0,
				   chStart = -1,
				   chlen = 0;
	public Word ()
	{}
	public Word(String name,String soundmark,String explain,String pos)
	{
		this.name = name;
		this.explain = explain;
		if(soundmark.equals("#") ) this.soundmark = "";
		else 
		this.soundmark = soundmark;
		setMp3Position(pos);
	}
	
	public void setMp3Position(String s)
	{
		String pos[] = s.split(" ");
		assert pos.length == 4;
		enStart = Integer.parseInt(pos[0]);
		enlen = Integer.parseInt(pos[1]);
		chStart = Integer.parseInt(pos[2]);
		chlen = Integer.parseInt(pos[3]);
	}
	
}
