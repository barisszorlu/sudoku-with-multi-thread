package sudokuwithmultithread;

public class YiginDugumu {
    private Lokasyon location;
    private int value;

    public YiginDugumu(Lokasyon location, int value) {
        this.location = location;
        this.value = value;
    }

    public Lokasyon getLocation() {
        return location;
    }

    public void setLocation(Lokasyon location) {
        this.location = location;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
