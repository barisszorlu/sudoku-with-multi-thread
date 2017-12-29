package sudokuwithmultithread;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/*
    bu thread sudokuyu sol üstten başlayıp satır satır gezerek
    çözüm arar. boş bulduğu kutucuğa sayı denerken 9'dan 1'e doğru ilerler.
*/
public class Thread2 extends Thread{
    private int dizi[][];
    private PrintWriter writer;
    
    private ThreadManager threadManager;
    static boolean signalled = false;
    private String result = "";
    
    private int xler, yler;
    
    private Yigin solutionStack;
    private String fileName;
    private boolean isInterrupted = false;
    
    public Thread2(String name, int dizi[][], ThreadManager threadManager) throws FileNotFoundException, UnsupportedEncodingException, IOException{
        this.dizi = dizi;
        writer = new PrintWriter("thread2.txt", "UTF-8");
        this.threadManager = threadManager;
        this.setName(name);
        solutionStack = new Yigin(81);
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public void run() {
        try {
            if(this.isInterrupted()) throw new InterruptedException();
            solve(dizi);
            
            if(!isInterrupted){
                setResult("En hızlı");
                threadManager.stopThreads();
                
            } else{
                setResult("Daha yavaş");
            }
            
            int size = solutionStack.size();
            YiginDugumu[] solution = new YiginDugumu[size];
            int index = size - 1;
            
            while (!solutionStack.isEmpty()) {
                YiginDugumu node = solutionStack.pop();
                Lokasyon l = node.getLocation();
                // System.out.println("(" + l.x + ", " + l.y + ") = " + node.getValue());
                solution[index] = node;
                index--;
            }
            
            for(int i = 0; i < size; i++){
                YiginDugumu node = solution[i];
                Lokasyon l = node.getLocation();
                String line = "(" + l.x + ", " + l.y + ")=" + node.getValue() + System.lineSeparator();
                writer.write(line);
            }
            
            writer.close();
            
        } catch(Exception e){
            synchronized(getClass()) {
                if (!signalled) {
                    signalled = true;
                    threadManager.stopThreads();
                }
            }
        }
    }
    
    public boolean solve(int dizi[][]) throws IOException, InterruptedException{
        if(this.isInterrupted()){
            isInterrupted = true;
            setResult("Daha yavaş");
            return true;
        }
        
        Lokasyon bosKutucuk = bosKutucukBul(dizi);
        int xler = bosKutucuk.x;
        int yler = bosKutucuk.y;
        
        if(xler == -1 && yler == -1){
            return true;
        }
        
        for(int sayi = 9; sayi >= 1; sayi--){
            if(guvenliMi(dizi, xler, yler, sayi)){
                dizi[xler][yler] = sayi;
                solutionStack.push(xler, yler, sayi);
                
                if(solve(dizi))
                    return true;
                
                dizi[xler][yler] = 0;
                solutionStack.pop();
            }
        }
        
        return false;
    }
    
    public Lokasyon bosKutucukBul(int dizi[][]){
        for(xler = 0; xler < 9; xler++){
            for(yler = 0; yler < 9; yler++){
                if(dizi[xler][yler] == 0){
                    return new Lokasyon(xler, yler);
                }
            }
        }
        return new Lokasyon(-1, -1);
    }
    
    public boolean satirdaVarMi(int dizi[][], int xler, int num){
        for(int yler = 0; yler < 9; yler++){
            if(dizi[xler][yler] == num){
                return true;
            }
        }
        return false;
    }
    
    public boolean sutundaVarMi(int dizi[][], int yler, int num){
        for(int xler = 0; xler < 9; xler++){
            if(dizi[xler][yler] == num){
                return true;
            }
        }
        return false;
    }
    
    public boolean kutudaVarMi(int dizi[][], int boxStartRow, int boxStartCol, int num){
        for (int xler = 0; xler < 3; xler++)
            for (int yler = 0; yler < 3; yler++)
                if (dizi[xler+boxStartRow][yler+boxStartCol] == num)
                    return true;
        return false;
    }
    
    public boolean guvenliMi(int dizi[][], int xler, int yler, int num){
        return !satirdaVarMi(dizi, xler, num) &&
               !sutundaVarMi(dizi, yler, num) &&
               !kutudaVarMi(dizi, xler - xler%3 , yler - yler%3, num);
    }
}
