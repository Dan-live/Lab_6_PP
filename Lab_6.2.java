package Lab_6_2try;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.Buffer;
import java.nio.file.FileSystems;
import java.util.concurrent.Semaphore;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.locks.*;
import java.util.Random;
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
    //public static int sum = 0;
    public static byte CR2_byte = 12;
    public static short CR2_short = 200;
    public static int CR2_int = 1800;
    public static long CR2_long = 890000L;
    public static float CR2_float = 1.7f;
    public static double CR2_double = 18.9;
    public static boolean CR2_boolean = true;
    public static char CR2_char = 'c';
    //public static int n = 0;

    //public static ReentrantLock mutex = new ReentrantLock();
}

class VectorBuffer {
    private static final int SIZE = 15;
   // private Elem[] vec = new Elem[SIZE];
    private int[] buff;
    private int ind1, ind2;
    public static int full_break;
    public static int empty_break;
    public static int elementsQuantity;
    private final FileWriter file;
    //public static int num_of_operations = 70;
    //public static int buff = new VectorBuffer();

    VectorBuffer(FileWriter file) {
        // for (int i = 0; i < SIZE; i++) {
        //     vec[i] = new Elem();
        // }
        ind1 = 0;
        ind2 = 0;
        elementsQuantity = 0;
        buff = new int[SIZE];
        this.file = file;
    }

    boolean isFull() {
        return ind2 >= SIZE;
    }

    boolean isEmpty() {
        return ind2 <= 0;
    }

    // boolean ValueSet = false;
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
        

        //System.out.println("Producer " + str +" in write to buffer[" + ind1 + "] value: " + data + "\n");
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
        //System.out.println("Consumer " + str +" get from buffer[" + ind2 + "] value: " + buff[ind2] + "\n");
        try {
            file.write("Consumer " + str + " get in buffer[" + ind2 + "] value: " + buff[ind2] + "\n");
        } catch (IOException e) {
            System.out.println("Consumer error: " + e);
        }
        //int result = vec[(ind1 - ind2 + 1 + SIZE) % SIZE].data;
        ind2 = (ind2 + 1) % buff.length;
        elementsQuantity--; 
        if(ind2 == 0) empty_break++;

        notify();
        //return vec[ind2];
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
        while (!VectorBuffer.isExit()){
            CR1.Get(thread.getName());
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
        while (!VectorBuffer.isExit()){
            try {
                SharedSemaphore.thread_sem1.release();
                file.write(thread.getName() + " opens semaphore SR2 for the Thread3\n");

                SharedSemaphore.thread_sem1.acquire();
                file.write(thread.getName() + " works AFTER synchronization point\n");

                CR1.Set(thread.getName());
            } catch (IOException | InterruptedException e) {
                System.out.println(thread.getName() + " error: " + e);
            }
        }
        if (!Barrier.br.isBroken()){
            Barrier.br.reset();
        }
        SharedSemaphore.thread_sem1.release();
        SharedSemaphore.thread_sem1.release();
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

        thread = new Thread(this, "P1");
        thread.start();
    }
    public Thread getThread(){
        return thread;
    }

    @Override
    public void run() {
        while (!VectorBuffer.isExit()){
            try {
                mutex.lock();
                CR2.CR2_byte = (byte)(CR2.CR2_byte + 3);
                CR2.CR2_short = (short)(CR2.CR2_byte + 10);
                CR2.CR2_int += 108;
                file.write(thread.getName() + " modified CR2_byte, CR2_short, CR2_int\n");
                mutex.unlock();

                Barrier.br.await();
                file.write(thread.getName() + " synchronized in barrier\n");

                mutex.lock();
                double sum = CR2.CR2_long + CR2.CR2_float + CR2.CR2_double;
                file.write(thread.getName() +" use (sum) CR2_long, CR2_float, CR2_double and get result: " + sum + "\n");
                mutex.unlock();

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
        while (!VectorBuffer.isExit()){
            CR1.Get(thread.getName());
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
        while (!VectorBuffer.isExit()){
            CR1.Set(thread.getName());
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

class P6 implements Runnable
{
    private final Thread thread;
    private final FileWriter file;
    private final ReentrantLock mutex;

    public P6(ReentrantLock mutex, FileWriter file)
    {
        this.mutex = mutex;
        this.file = file;

        thread = new Thread(this, "Thread6");
        thread.start();
    }
    public Thread getThread(){
        return thread;
    }

    @Override
    public void run() {
        while (!VectorBuffer.isExit()){
            try {

                Barrier.br.await();
                file.write(thread.getName() + " synchronized in barrier\n");

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


class Main
{
    public static void main(String[] args)
    {
        try{
            FileWriter file = new FileWriter("log/result.log");
            ReentrantLock mutex = new ReentrantLock();
            VectorBuffer CR1 = new VectorBuffer(file);
            

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