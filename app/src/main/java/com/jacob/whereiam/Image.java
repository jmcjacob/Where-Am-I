package com.jacob.whereiam;

public class Image {
    protected String Title;
    protected String SRC;
    protected static final String TITLE_PREFIX = "Title_";
    protected static final String SRC_PREFIX = "SRC_";

    public Image(String title, String src)
    {
        this.Title = title;
        this.SRC = src;
    }
}
