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

class Elem {
    int data;
}
class File
{
    public static FileWriter file;
}
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
    public static int sum = 0;
    //public static int n = 0;

    public static ReentrantLock mutex = new ReentrantLock();
}
class VectorBuffer {
    private static final int SIZE = 15;
   // private Elem[] vec = new Elem[SIZE];
    private int[] buff;
    private int ind1, ind2;
    public static int full_break;
    public static int empty_break;
    public static int elementsQuantity;
    //public static int num_of_operations = 70;
    //public static int buff = new VectorBuffer();

    VectorBuffer() {
        // for (int i = 0; i < SIZE; i++) {
        //     vec[i] = new Elem();
        // }
        ind1 = 0;
        ind2 = 0;
        elementsQuantity = 0;
        buff = new int[SIZE];
    }

    boolean isFull() {
        return ind2 >= SIZE;
    }

    boolean isEmpty() {
        return ind2 <= 0;
    }

    // boolean ValueSet = false;
    synchronized void Set(int data, String str) {
       
        while(elementsQuantity == buff.length ){
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Producer error: " + e);
            }
        }
        buff[ind1] = data;
        

        System.out.println("Producer " + str +" in write to buffer[" + ind1 + "] value: " + data + "\n");



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
        System.out.println("Consumer " + str +" get from buffer[" + ind2 + "] value: " + buff[ind2] + "\n");
        
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

class P1 extends Thread
{
    int num ;
    private final VectorBuffer vect;
    // private P2 thread2;
    // private P3 thread3;
    // private P4 thread4;
    // private P5 thread5;
    // private P6 thread6;

    P1(VectorBuffer CR1) 
    {
        super("P1");
        this.vect = CR1;
        // this.thread2 = thread2;
        // this.thread3 = thread3;
        // this.thread4 = thread4;
        // this.thread5 = thread5;
        // this.thread6 = thread6;
        start();
    }
    public void run() 
    {
        System.out.println("Thread 1 is started\n");
        while(!VectorBuffer.isExit())
        {
            //if (VectorBuffer.buff.isExit() || VectorBuffer.num_of_operations <= 0) break;
            
                //System.out.println("Thread 1 get value \n");
                vect.Get(getName());
                //VectorBuffer.buff.Get();
            
            // try {
            //     Thread.sleep(10);
            // } catch (InterruptedException e) {
            //     e.printStackTrace();
            // }
           
            

        }

        if (!Barrier.br.isBroken()){
            Barrier.br.reset();
        }
        SharedSemaphore.thread_sem1.release();
        SharedSemaphore.thread_sem1.release();
        System.out.println("P1 finished!\n");

        // thread2.interrupt();
        // System.out.println("p1| p2 canceled\n");
        // thread3.interrupt();
        // System.out.println("p1| p3 canceled\n");
        // thread4.interrupt();
        // System.out.println("p1| p4 canceled\n");
        // thread5.interrupt();
        // System.out.println("p1| p5 canceled\n");
        // thread6.interrupt();
        // System.out.println("p1| p6 canceled\n");

    }
}

class P2 extends Thread
{
    private final VectorBuffer vect;
    P2(VectorBuffer CR1) 
    {
        super("P2");
        this.vect = CR1;
        start();
    }
    public void run() 
    {
        System.out.println("Thread 2 is started\n");
        while(!VectorBuffer.isExit())
        {
            
                
            //if (VectorBuffer.buff.isExit() || VectorBuffer.num_of_operations <= 0) break;
            //повна синхронізація Sem1 + Sem2

            System.out.println("\nThread_2 opens semaphore thread_sem2 for the Thread_3");
            /* Функція відкриває семафор thread_sem2 для потоку Thread_2 */
            SharedSemaphore.thread_sem2.release();
 
            System.out.println("Semaphore thread_sem2 is opened!");
 
            System.out.println("Thread_2 waits for the opening of the semaphore thread_sem1");
            try
            {
               /* Функція чекає, поки потік Thread_2 відкриє семафор thread_sem1. */
                SharedSemaphore.thread_sem1.acquire();
 
           }catch(InterruptedException e)
               {
                   System.out.println("Thread_2 interrupted");
               }
 
            System.out.println("\nThread_2 works AFTER synchronization point.");

            //поклав 
            Random rand = new Random();
            //System.out.println("Thread 2 set value \n");
            vect.Set(rand.nextInt(1000), getName());
            // System.out.println("Thread 2 set value " + VectorBuffer.buff.Set(rand.nextInt(1000)) + " to CR1\n");
            // if(VectorBuffer.buff.isExit()) VectorBuffer.full_break++;
            // VectorBuffer.num_of_operations--;
            
            
            
        }
        
        if (!Barrier.br.isBroken()){
            Barrier.br.reset();
        }
        SharedSemaphore.thread_sem1.release();
        SharedSemaphore.thread_sem1.release();
        System.out.println("P2 finished!\n");
    }


}

class P3 extends Thread
{

    //private CyclicBarrier br;
    P3(/*CyclicBarrier brInit*/) 
    {
        super("P3");
        //br = brInit;
        start();
    }
    public void run() 
    {
        System.out.println("Thread 3 is started");
        while(!VectorBuffer.isExit())
        {
            //if (VectorBuffer.buff.isExit() || VectorBuffer.num_of_operations <= 0) break;

            //модивікація змінних
            
            CR2.mutex.lock();
            System.out.println("Thread 3 locked mutex");
            CR2.sum = CR2.sum + 7;
            System.out.println("sum = " + CR2.sum + ";  i = " + 7);
			
            System.out.println("Thread 3 unlocked mutex");
			CR2.mutex.unlock();

            //циклічний бар'єр 

            System.out.println("\nThread_3  waits until Thread_6 reaches synchronization point");
            try
            {
              /* Потік 2 чекає, доки потік 1 дійде до точки синхронізації */  
             Barrier.br.await();
              
            }catch(InterruptedException e)
                {
                System.out.println(e.getMessage());
                }
             catch(BrokenBarrierException e)
                  {
                 System.out.println(e.getMessage());
                  }

            //використання змінних

            CR2.mutex.lock();
            System.out.println("Thread 3 locked mutex");
            int num_of_sum = CR2.sum + 44;
            System.out.println("num_of_sum = " + num_of_sum);
            System.out.println("Thread 3 unlocked mutex");
			CR2.mutex.unlock();

            //повна синхронізація Sem1 + Sem2
                 
		    System.out.println("\nThread_3 opens semaphore thread_sem1 for the Thread_2");
		    /* Функція відкриває семафор thread_sem1 для потоку Thread_1 */
		     SharedSemaphore.thread_sem1.release();

		     System.out.println("Semaphore thread_sem1 is opened!");

		     System.out.println("Thread_3 waits for the opening of the semaphore thread_sem2");
 		     try
 		     {
 			      /* Функція чекає, поки потік Thread_1 відкриє семафор thread_sem2. */
 		    	  SharedSemaphore.thread_sem2.acquire();

		    }catch(InterruptedException e)
 			{
 				System.out.println("Thread_3 interrupted");
 		  	}

 		     System.out.println("\nThread_3 works AFTER semaphore thread_sem2");
              //VectorBuffer.num_of_operations--;
              
        }
            
            
          if (!Barrier.br.isBroken()){
            Barrier.br.reset();
          }
          SharedSemaphore.thread_sem1.release();
          SharedSemaphore.thread_sem1.release();
          System.out.println("P3 finished!\n");
    }

}


class P4 extends Thread
{

    //int num ;
    private final VectorBuffer vect;
    P4(VectorBuffer CR1) 
    {
        super("P4");
        this.vect = CR1;
        start();
    }
    public void run() 
    {
        System.out.println("Thread 4 is started\n");
        while(!VectorBuffer.isExit())
        {
            //if (VectorBuffer.buff.isExit() || VectorBuffer.num_of_operations <= 0) break;
            
                //System.out.println("Thread 4 get value \n");
                vect.Get(getName());
                //VectorBuffer.buff.Get();
            
            // try {
            //     Thread.sleep(10);
            // } catch (InterruptedException e) {
            //     e.printStackTrace();
            // }
            
            
        }
        
        if (!Barrier.br.isBroken()){
            Barrier.br.reset();
        }
        SharedSemaphore.thread_sem1.release();
        SharedSemaphore.thread_sem1.release();
        System.out.println("P4 finished!\n");

    }
}

class P5 extends Thread
{
    private final VectorBuffer vect;
    P5(VectorBuffer CR1) 
    {
        super("P5");
        this.vect = CR1;
        start();
    }
    public void run() 
    {
        System.out.println("Thread 5 is started\n");
        while(!VectorBuffer.isExit())
        {
            //if (VectorBuffer.buff.isExit() || VectorBuffer.num_of_operations <= 0) break;
            
                Random rand = new Random();
               // System.out.println("Thread 5 set value \n");
                vect.Set(rand.nextInt(1000), getName());
                //VectorBuffer.buff.Get();
            
            // try {
            //     Thread.sleep(10);
            // } catch (InterruptedException e) {
            //     e.printStackTrace();
            // }
            
            
        }
        
        if (!Barrier.br.isBroken()){
            Barrier.br.reset();
        }
        SharedSemaphore.thread_sem1.release();
        SharedSemaphore.thread_sem1.release();
        System.out.println("P5 finished!\n");
    }
}

class P6 extends Thread
{


    //private CyclicBarrier br;
    P6(/*CyclicBarrier brInit*/) 
    {
        super("P6");
        //br = brInit;
        start();
    }
    public void run() 
    {
        System.out.println("Thread 6 is started\n");
        while(!VectorBuffer.isExit())
        {
            //if (VectorBuffer.isExit() || VectorBuffer.num_of_operations <= 0) break;
            
            //циклічний бар'єр
            System.out.println("\nThread_6  waits until Thread_3 reaches synchronization point");
            try
            {
              /* Потік 2 чекає, доки потік 1 дійде до точки синхронізації */  
              Barrier.br.await();
              
            }catch(InterruptedException e)
                {
                System.out.println(e.getMessage());
                }
             catch(BrokenBarrierException e)
                  {
                 System.out.println(e.getMessage());
                  }

            //використання змінних

            CR2.mutex.lock();
            System.out.println("Thread 6 locked mutex");
            int num_of_sum = CR2.sum + 27;
            System.out.println("num_of_sum = " + num_of_sum);
            System.out.println("Thread 6 unlocked mutex");
			CR2.mutex.unlock();
            //VectorBuffer.num_of_operations--;

        }
        
        if (!Barrier.br.isBroken()){
            Barrier.br.reset();
        }
        SharedSemaphore.thread_sem1.release();
        SharedSemaphore.thread_sem1.release();
        System.out.println("P6 finished!\n");

    }
}

class Main {
    

    public static void main(String[] args) {

        //int first_length = 4;
        //Random rand = new Random();

        // for (int i = 0; i < first_length; i++) {
        //     System.out.println(VectorBuffer.buff.Set(rand.nextInt(1000)));
        // }

        //System.out.println("Array filled by elements from 0-th to " + (first_length - 1)+ "\n");


        
     

        try
        {
            FileWriter file = new FileWriter("log/result.log");
            VectorBuffer CR1 = new VectorBuffer();
            P1 thread1 = new P1(CR1);
            P2 thread2 = new P2(CR1);
            P3 thread3 = new P3(/*Barrier.br*/);
            P4 thread4 = new P4(CR1);
            P5 thread5 = new P5(CR1);
            P6 thread6 = new P6(/*Barrier.br*/);

            thread1.join();
            thread2.join();
            thread3.join();
            thread4.join();
            thread5.join();
            thread6.join();
            
            System.out.println("Main finished");
            file.write("Main finished");
            file.close();
        }
        catch(IOException | InterruptedException e)
        {
            System.out.println("Main thred intrerapted");
        }

    }
}
