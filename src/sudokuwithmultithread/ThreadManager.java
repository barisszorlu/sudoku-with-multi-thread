package sudokuwithmultithread;

import java.util.ArrayList;

public class ThreadManager<T> extends ArrayList<T>{
    public void stopThreads() {
        for (T t : this) {
            Thread thread = (Thread) t;
            if (thread.isAlive()) {
                try { thread.interrupt(); } 
                catch (Exception e) {
                    
                }
            }
        }
    }
}
