package sudokuwithmultithread;

public class Yigin {
    private int maxSize;
    private YiginDugumu[] stackArray;
    private int top;
    
    
   
    public Yigin(int s) {
        maxSize = s;
        stackArray = new YiginDugumu[maxSize];
        top = -1;
    }
    
    public void push(int x, int y, int value) {
        YiginDugumu newNode = new YiginDugumu(new Lokasyon(x, y), value);
        stackArray[++top] = newNode;
    }
    
    public YiginDugumu pop() {
        return stackArray[top--];
    }
    
    public YiginDugumu peek() {
        return stackArray[top];
    }
    
    public boolean isEmpty() {
        return (top == -1);
    }
    
    public boolean isFull() {
        return (top == maxSize - 1);
    }
    
    public int size(){
        return top+1;
    }
}
