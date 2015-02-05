package edu.uml.swin.sleepfit;


public class LifestyleItem {
    private int icon;
    private String type;

    public LifestyleItem() {}

    public LifestyleItem(String type, int icon) {
        this.type = type;
        this.icon = icon;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
