package jacob.com.whereami;

public class Image {
    protected String Title;
    protected String SRC;
    protected String ID;
    public Image(String title, String id)   {
        this.Title = title;
        this.ID = id;
    }
    public void setSRC(String src)  {
        this.SRC = src;
    }
}
