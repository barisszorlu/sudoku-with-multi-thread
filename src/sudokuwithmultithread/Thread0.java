package sudokuwithmultithread;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/*
    bu thread sudokuyu sol üstten başlayıp satır satır gezerek
    çözüm arar. boş bulduğu kutucuğa sayı denerken 1'den 9'a doğru ilerler.
*/
public class Thread0 extends Thread{
    // çözülecek sudoku dizisi
    private int dizi[][];
    
    // dosyaya yazacak olan writer
    private PrintWriter writer;
    
    // tüm threadlerin içinde bulunuyor olacağı threadManager
    private ThreadManager<Thread> threadManager;
    static boolean signalled = false;
    private String result = "";
    
    private int xler, yler;
    
    private Yigin solutionStack;
    private boolean isInterrupted = false;
    
    public Thread0(String name, int dizi[][], ThreadManager threadManager) throws FileNotFoundException, UnsupportedEncodingException, IOException{
        this.dizi = dizi;
        writer = new PrintWriter("thread0.txt", "UTF-8");
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

    public Yigin getSolutionStack() {
        return solutionStack;
    }

    public void setSolutionStack(Yigin solutionStack) {
        this.solutionStack = solutionStack;
    }
    
    @Override
    public void run() {
        try {
            if(this.isInterrupted()) throw new InterruptedException();
            
            // tüm çözme işlemini gerçekleştirecek olan metod
            solve(dizi);
            
            // thread kaybeden de kazanan da olsa bu satıra gelinecek
            // fakat kaybettiyse biz isInterrupted isimli bayrağı true yapmış olacağı
            if(!isInterrupted){
                setResult("En hızlı");
                threadManager.stopThreads();
                
            } else{
                setResult("Daha yavaş");
            }
            
            // buradan aşağısı stack içerine biriktirdiğimiz adımları
            // dosyaya yazmak için kullanılıyor
            // stack kullanıyoruz çünkü sudoku çözme algoritmamız
            // deneme yanılma yöntemine dayandığından
            // on kutunun içini doldurup bir çıkmaza girdiğimizde
            // onunu da geri silebilme ihtimalimiz var
            // kutuları doldururken adımları text dosyasına yazmaktansa
            // kutu doldurma işleminde stacke push
            // boşaltma işleminde ise stackden pop ediyoruz
            // böylece işlem bittiğinde stack içerisinde saf çözüm kalmış oluyor
            
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
    
    // solve metodu recursive bir şekilde çağırılacak
    public boolean solve(int dizi[][]) throws IOException, InterruptedException{
        // her çağrıldığında içinde bulunduğumuz thread
        // durduruldu mu diye kontrol ediyoruz
        if(this.isInterrupted()){
            // eğer durdurulduysa bayrağı true'ya çeviriyoruz
            isInterrupted = true;
            // sonucumuzu kaybeden olarak belirliyoruz
            setResult("Daha yavaş");
            // ve true döndürerek solve metodunun sonlanmasını sağlıyoruz
            return true;
        }
        
        // bosKutucukBul metodundan değeri 0 olan metodun lokasyonu alınır
        Lokasyon bosKutucuk = bosKutucukBul(dizi);
        
        // bu lokasyon xler ve yler değişkenlerine gönderilir
        int xler = bosKutucuk.x;
        int yler = bosKutucuk.y;
        
        // eğer xler ve yler değerleri -1 geldiyse boş kutucuk kalmamış demektir
        // sudokunun çözüldüğü manasına gelir, true döndürüyoruz
        if(xler == -1 && yler == -1){
            return true;
        }
        
        // boş kutucuğa 1'den 9'a kadar sayıları sırasıyla deniyoruz
        for(int sayi = 1; sayi <= 9; sayi++){
            // her sayı için o sayının güvenli olup olmadığını kontrol ediyoruz
            if(guvenliMi(dizi, xler, yler, sayi)){
                
                // güvenliyse diziye sayıyı yazıyoruz
                dizi[xler][yler] = sayi;
                
                // ve stack içerisine push işlemi
                solutionStack.push(xler, yler, sayi);
                
                // sayıyı yazdıktan sonra solve metodunu yeni sayılı haliyle tekrar çağırıyoruz
                // buradan true false dönerse, fonksiyon bir aşağıya geçecek ve az önce yazdığımız değeri
                // 0'a çevirip for döngüsünün bir sonraki sayıya geçmesini sağlayacak
                // çünkü false dönmesi orada sıkıştığımız manasına geliyor
                if(solve(dizi))
                    return true;
                
                // sıkıştıysak pop ediyoruz ve 0 yapıyoruz
                dizi[xler][yler] = 0;
                solutionStack.pop();
            }
        }
        
        return false;
    }
    
    // bu metod diziyi sol üstten satır satır okur ve 0 değeri gördüğünde lokasyon döndürür
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
    
    // bu metod dizinin ilgili sütunundaki satırların verilen sayıyı içerip içermediğine bakar
    public boolean satirdaVarMi(int dizi[][], int xler, int num){
        for(int yler = 0; yler < 9; yler++){
            if(dizi[xler][yler] == num){
                return true;
            }
        }
        return false;
    }
    
    // bu metod dizinin ilgili satırındaki sütunların verilen sayıyı içerip içermediğine bakar
    public boolean sutundaVarMi(int dizi[][], int yler, int num){
        for(int xler = 0; xler < 9; xler++){
            if(dizi[xler][yler] == num){
                return true;
            }
        }
        return false;
    }
    
    // bu metod ilgili 3x3 kutucuğun sayıyı içerip içermediğine bakar
    public boolean kutudaVarMi(int dizi[][], int boxStartRow, int boxStartCol, int num){
        for (int xler = 0; xler < 3; xler++)
            for (int yler = 0; yler < 3; yler++)
                if (dizi[xler+boxStartRow][yler+boxStartCol] == num)
                    return true;
        return false;
    }
    
    // bu metod yukarıdaki üç metoda göre güvenli olup olmadığını döndürür
    public boolean guvenliMi(int dizi[][], int xler, int yler, int num){
       
        return !satirdaVarMi(dizi, xler, num) &&
               !sutundaVarMi(dizi, yler, num) &&
               !kutudaVarMi(dizi, xler - xler%3 , yler - yler%3, num);
    }
    
}