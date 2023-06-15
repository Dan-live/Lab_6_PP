import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.locks.*;

class Barrier 
{
    public static  CyclicBarrier br = new CyclicBarrier(2);
}


class SharedSemaphore
{
	/* Ініціалізація семафорів */
	public static Semaphore thread_sem1 = new Semaphore(0, true);
	public static Semaphore thread_sem2 = new Semaphore(0, true);
}

class CR2{
    public static byte CR2_byte = 34;
    public static short CR2_short = 100;
    public static int CR2_int = 3131;
    public static long CR2_long = 212000L;
    public static float CR2_float = 46.5f;
    public static double CR2_double = 324.9;
    public static boolean CR2_boolean = true;
    public static char CR2_char = 'c';
}

class VectorBuffer {
    private static final int SIZE = 15;
    private int[] buff;
    private int ind1, ind2;
    public static int full_break;
    public static int empty_break;
    public static int elementsQuantity;
    private final FileWriter file;

    VectorBuffer(FileWriter file) {
        buff = new int[SIZE];
        ind1 = 0;
        ind2 = 0;
        elementsQuantity = 0;
        this.file = file;
    }

    synchronized void Set(String str) {
       
        while(elementsQuantity == buff.length ){
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Producer error: " + e);
            }
        }

        int num = (int)(Math.random() * 100);

        buff[ind1] = num;
        
        
        try {
            file.write("Producer " + str + " in write to buffer[" + ind1 + "] value: " + num + "\n");
        } catch (IOException e) {
            System.out.println("Producer error: " + e);
        }


        ind1 = (ind1 + 1) % buff.length;
        elementsQuantity++;
        if(ind1 == 0) full_break++;
        notify();

    }

    synchronized void Get(String str) {

        while(elementsQuantity == 0){
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Consumer error: " + e);
            }
        }
        
        try {
            file.write("Consumer " + str + " get from buffer[" + ind2 + "] value: " + buff[ind2] + "\n");
        } catch (IOException e) {
            System.out.println("Consumer error: " + e);
        }
        
        ind2 = (ind2 + 1) % buff.length;
        elementsQuantity--; 
        if(ind2 == 0) empty_break++;

        notify();
        
    }

    public static boolean isExit() {
        return (full_break >= 2 && empty_break >= 2);
    }
}


class P1 implements Runnable
{
    private final VectorBuffer CR1;
    private final Thread thread;
    private final FileWriter file;

    public P1(VectorBuffer CR1, FileWriter file)
    {
        this.CR1 = CR1;
        this.file = file;
        thread = new Thread(this, "P1");
        thread.start();
    }

    public Thread getThread(){
        return thread;
    }   


    @Override
    public void run() {
        try{
            file.write(thread.getName() + " started!\n");
        while (!VectorBuffer.isExit()){
            //забираємо данні з буферу 
            CR1.Get(thread.getName());
        }
        } catch (IOException  e) {
             System.out.println(thread.getName() + " error: " + e);
        }

        try {
            file.write(thread.getName() + " finished!\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}

class P2 implements Runnable
{
    private final VectorBuffer CR1;
    private final Thread thread;
    private final FileWriter file;

    public P2(VectorBuffer CR1, FileWriter file)
    {
        this.CR1 = CR1;
        this.file = file;
        thread = new Thread(this, "P2");
        thread.start();
    }
    public Thread getThread(){
        return thread;
    }
    @Override
    public void run() {
        try{
            file.write(thread.getName() + " started!\n");
        while (!VectorBuffer.isExit()){
            try {
                //повна синхронізація за допомогою 2 семафорів з потоком Р3 
                SharedSemaphore.thread_sem2.release();
                file.write(thread.getName() + " opens semaphore SR2 for the Thread3\n");

                SharedSemaphore.thread_sem1.acquire();
                file.write(thread.getName() + " works AFTER synchronization point\n");
                
                //записуємо занчення у буфер
                CR1.Set(thread.getName());
            } catch (IOException | InterruptedException e) {
                System.out.println(thread.getName() + " error: " + e);
            }
        }
      } catch (IOException e) {
            System.out.println(thread.getName() + " error: " + e);
      }


        SharedSemaphore.thread_sem1.release();
        SharedSemaphore.thread_sem2.release();
      try {
        file.write(thread.getName() + " finished!\n");
    } catch (IOException e) {
        throw new RuntimeException(e);
    }

    }
        
    

}



class P3 implements Runnable
{
    private final Thread thread;
    private final FileWriter file;
    private final ReentrantLock mutex;

    public P3(ReentrantLock mutex, FileWriter file)
    {
        this.mutex = mutex;
        this.file = file;

        thread = new Thread(this, "P3");
        thread.start();
    }
    public Thread getThread(){
        return thread;
    }

    @Override
    public void run() {
        try{
            file.write(thread.getName() + " started!\n");
        while (!VectorBuffer.isExit()){
            try {
                //модифікація змінних
                mutex.lock();
                CR2.CR2_byte = (byte)(CR2.CR2_byte + 3);
                CR2.CR2_short = (short)(CR2.CR2_byte + 11);
                CR2.CR2_int += 99;
                file.write(thread.getName() + " modified CR2_byte, CR2_short, CR2_int\n");
                mutex.unlock();

                //синхронізація Р3 і Р2 за допомогою бар'єра  
                Barrier.br.await();
                file.write(thread.getName() + " synchronized in barrier\n");

                //використання змінних
                mutex.lock();
                double sum = CR2.CR2_long + CR2.CR2_float + CR2.CR2_double;
                file.write(thread.getName() +" use (sum) CR2_long, CR2_float, CR2_double and get result: " + sum + "\n");
                mutex.unlock();

                //повна синхронізація за допомогою 2 семафорів з потоком Р2 
                SharedSemaphore.thread_sem1.release();
                file.write(thread.getName() + " opens semaphore SR1 for the Thread2\n");

                SharedSemaphore.thread_sem2.acquire();
                file.write(thread.getName() + " works AFTER synchronization point\n");

            } catch (IOException | InterruptedException e) {
                System.out.println(thread.getName() + " error: " + e);
            } catch (BrokenBarrierException e){
                break;
            }
        }
     } catch (IOException e) {
        System.out.println(thread.getName() + " error: " + e);
     }

     if (!Barrier.br.isBroken()){
        Barrier.br.reset();
        }
        SharedSemaphore.thread_sem1.release();
        SharedSemaphore.thread_sem2.release();
        try {
            file.write(thread.getName() + " finished!\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}

class P4 implements Runnable
{
    private final VectorBuffer CR1;
    private final Thread thread;
    private final FileWriter file;

    public P4(VectorBuffer CR1, FileWriter file)
    {
        this.CR1 = CR1;
        this.file = file;
        thread = new Thread(this, "P4");
        thread.start();
    }

    public Thread getThread(){
        return thread;
    }   


    @Override
    public void run() {
        try{
            file.write(thread.getName() + " started!\n");
        while (!VectorBuffer.isExit()){
            //забираємо данні з буферу 
            CR1.Get(thread.getName());
        }
        } catch (IOException e) {
          System.out.println(thread.getName() + " error: " + e);
        }

    }

}

class P5 implements Runnable
{
    private final VectorBuffer CR1;
    private final Thread thread;
    private final FileWriter file;

    public P5(VectorBuffer CR1, FileWriter file)
    {
        this.CR1 = CR1;
        this.file = file;
        thread = new Thread(this, "P5");
        thread.start();
    }

    public Thread getThread(){
        return thread;
    }   


    @Override
    public void run() {
        try{
            file.write(thread.getName() + " started!\n");
        while (!VectorBuffer.isExit()){
            //записуємо данні в буфер
            CR1.Set(thread.getName());
        }
     } catch (IOException e) {
        System.out.println(thread.getName() + " error: " + e);
    }


    try {
        file.write(thread.getName() + " finished!\n");
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
    }
}

class P6 implements Runnable
{
    private final Thread thread;
    private final FileWriter file;
    private final ReentrantLock mutex;

    public P6(ReentrantLock mutex, FileWriter file)
    {
        this.mutex = mutex;
        this.file = file;

        thread = new Thread(this, "P6");
        thread.start();
    }
    public Thread getThread(){
        return thread;
    }

    @Override
    public void run() {
        try{
            file.write(thread.getName() + " started!\n");
        while (!VectorBuffer.isExit()){
            try {
                //синхронізація Р3 і Р2 за допомогою бар'єра
                Barrier.br.await();
                file.write(thread.getName() + " synchronized in barrier\n");

                //використання змінних 
                mutex.lock();
                if(CR2.CR2_boolean){
                    CR2.CR2_char = 'a';
                }
                else {
                    CR2.CR2_char = 'b';
                }
                file.write(thread.getName() + " use CR2_boolean, CR2_char\n");
                mutex.unlock();

            } catch (IOException | InterruptedException e) {
                System.out.println(thread.getName() + " error: " + e);
            } catch (BrokenBarrierException e){
                break;
            }
        }
    } catch (IOException e) {
        System.out.println(thread.getName() + " error: " + e);
    }

        if (!Barrier.br.isBroken()){
        Barrier.br.reset();
        }
        try {
            file.write(thread.getName() + " finished!\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


}


class Main
{
    public static void main(String[] args)
    {
        try{
            FileWriter file = new FileWriter("log/result.log");
            
            VectorBuffer CR1 = new VectorBuffer(file);

            ReentrantLock mutex = new ReentrantLock();

            P1 thread1 = new P1(CR1, file);
            P2 thread2 = new P2(CR1, file);
            P3 thread3 = new P3(mutex, file);
            P4 thread4 = new P4(CR1, file);
            P5 thread5 = new P5(CR1, file);
            P6 thread6 = new P6(mutex, file);

            thread1.getThread().join();
            thread2.getThread().join();
            thread3.getThread().join();
            thread4.getThread().join();
            thread5.getThread().join();
            thread6.getThread().join();

            file.write("Main finished");
            file.close();

        } catch (IOException | InterruptedException e) {
            System.out.println("Main error: " + e);
        }
    }
}