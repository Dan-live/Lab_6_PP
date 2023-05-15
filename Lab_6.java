import java.util.concurrent.Semaphore;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.locks.*;



import java.util.Random;

class Elem {
    int data;
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
    private Elem[] vec = new Elem[SIZE];
    private int ind1, ind2;
    public static int full_break = 0;
    public static int empty_break = 0;
    public static int num_of_operations = 3;
    public static VectorBuffer buff = new VectorBuffer();

    VectorBuffer() {
        for (int i = 0; i < SIZE; i++) {
            vec[i] = new Elem();
        }
    }

    boolean isFull() {
        return ind2 >= SIZE;
    }

    boolean isEmpty() {
        return ind2 <= 0;
    }

    synchronized int Set(int data) {
        if (ind2 != 0) {
            ind1 = (ind1 + 1) % SIZE;
        }
        vec[ind1].data = data;
        ind2++;
        return data;
    }

    synchronized int Get() {
        int result = vec[(ind1 - ind2 + 1 + SIZE) % SIZE].data;
        ind2--;
        return result;
    }

     boolean isExit() {
        return full_break >= 2 && empty_break >= 2;
    }
}

class P1 extends Thread
{
    
    private CyclicBarrier br;
    int num ;
    P1() 
    {
        super("P1");
        start();
    }
    public void run() 
    {
        System.out.println("Thread 1 is started\n");
        while(true)
        {
            if (VectorBuffer.buff.isExit() || VectorBuffer.num_of_operations <= 0) break;
            System.out.println("Thread 1 get value " + VectorBuffer.buff.Get() + " from CR1\n");
            if(VectorBuffer.buff.isExit()) VectorBuffer.empty_break++;
            VectorBuffer.num_of_operations--;

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        // P2 thread2 = new P2();
        // thread2.interrupt();
        // System.out.println("p1| p2 canceled\n");
        // P3 thread3 = new P3(br);
        // thread3.interrupt();
        // System.out.println("p1| p3 canceled\n");
        // P4 thread4 = new P4();
        // thread4.interrupt();
        // System.out.println("p1| p4 canceled\n");
        // P5 thread5 = new P5();
        // thread5.interrupt();
        // System.out.println("p1| p5 canceled\n");
        // P6 thread6 = new P6(br);
        // thread6.interrupt();
        // System.out.println("p1| p6 canceled\n");

    }
}

class P2 extends Thread
{
    private static VectorBuffer buff = new VectorBuffer();
    private CyclicBarrier br;
    P2() 
    {
        super("P2");
        start();
    }
    public void run() 
    {
        System.out.println("Thread 2 is started\n");
        while(true)
        {
            if (buff.isExit() || VectorBuffer.num_of_operations <= 0) break;
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
            int num = rand.nextInt(1000);
            buff.Set(num);
            System.out.println("Thread 2 set value " + num + " to CR1\n");
            if(buff.isExit()) VectorBuffer.full_break++;
            VectorBuffer.num_of_operations--;
        }
        // P1 thread1 = new P1();
        // thread1.interrupt();
        // System.out.println("p2| p1 canceled\n");
        // P3 thread3 = new P3(br);
        // thread3.interrupt();
        // System.out.println("p2| p3 canceled\n");
        // P4 thread4 = new P4();
        // thread4.interrupt();
        // System.out.println("p2| p4 canceled\n");
        // P5 thread5 = new P5();
        // thread5.interrupt();
        // System.out.println("p2| p5 canceled\n");
        // P6 thread6 = new P6(br);
        // thread6.interrupt();
        // System.out.println("p2| p6 canceled\n");
    }


}

class P3 extends Thread
{
    private static VectorBuffer buff = new VectorBuffer();
    private CyclicBarrier br;
    P3(CyclicBarrier brInit) 
    {
        super("P3");
        br = brInit;
        start();
    }
    public void run() 
    {
        System.out.println("Thread 3 is started");
        while(true)
        {
            if (buff.isExit() || VectorBuffer.num_of_operations <= 0) break;

            //модивікація змінних
            
            CR2.mutex.lock();
            System.out.println("Thread 3 locked mutex");
            CR2.sum = CR2.sum + 7;
            System.out.println("sum = " + CR2.sum + ";  i = " + 7);
			
            System.out.println("Thread 3 unlocked mutex");
			CR2.mutex.unlock();

            //циклічний бар'єр 

            System.out.println("\nThread_2  waits until Thread_1 reaches synchronization point");
            try
            {
              /* Потік 2 чекає, доки потік 1 дійде до точки синхронізації */  
              br.await();
              
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
              VectorBuffer.num_of_operations--;

        }
        // P1 thread1 = new P1();
        // thread1.interrupt();
        // System.out.println("p3| p1 canceled\n");
        // P2 thread2 = new P2();
        // thread2.interrupt();
        // System.out.println("p3| p2 canceled\n");
        // P4 thread4 = new P4();
        // thread4.interrupt();
        // System.out.println("p3| p4 canceled\n");
        // P5 thread5 = new P5();
        // thread5.interrupt();
        // System.out.println("p3| p5 canceled\n");
        // P6 thread6 = new P6(br);
        // thread6.interrupt();
        // System.out.println("p3| p6 canceled\n");
    }
}

class P4 extends Thread
{
    //private static VectorBuffer buff = new VectorBuffer();
    private CyclicBarrier br;
    int num ;
    P4() 
    {
        super("P4");
        start();
    }
    public void run() 
    {
        System.out.println("Thread 4 is started\n");
        while(true)
        {
            if (VectorBuffer.buff.isExit() || VectorBuffer.num_of_operations <= 0) break;
            System.out.println("Thread 4 get value " +  VectorBuffer.buff.Get() + " from CR1\n");
            if(VectorBuffer.buff.isExit()) VectorBuffer.empty_break++;
            VectorBuffer.num_of_operations--;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        // P1 thread1 = new P1();
        // thread1.interrupt();
        // System.out.println("p4| p1 canceled\n");
        // P2 thread2 = new P2();
        // thread2.interrupt();
        // System.out.println("p4| p2 canceled\n");
        // P3 thread3 = new P3(br);
        // thread3.interrupt();
        // System.out.println("p4| p3 canceled\n");
        // P5 thread5 = new P5();
        // thread5.interrupt();
        // System.out.println("p4| p5 canceled\n");
        // P6 thread6 = new P6(br);
        // thread6.interrupt();
        // System.out.println("p4| p6 canceled\n");
    }
}

class P5 extends Thread
{
    private static VectorBuffer buff = new VectorBuffer();
    private CyclicBarrier br;
    P5() 
    {
        super("P5");
        start();
    }
    public void run() 
    {
        System.out.println("Thread 5 is started\n");
        while(true)
        {
            if (buff.isExit() || VectorBuffer.num_of_operations <= 0) break;
            Random rand = new Random();
            int num = rand.nextInt(1000);
            buff.Set(num);
            System.out.println("Thread 5 set value " + num + " to CR1\n");
            if(buff.isExit()) VectorBuffer.full_break++;
            VectorBuffer.num_of_operations--;
        }
        // P1 thread1 = new P1();
        // thread1.interrupt();
        // System.out.println("p5| p1 canceled\n");
        // P2 thread2 = new P2();
        // thread2.interrupt();
        // System.out.println("p5| p2 canceled\n");
        // P3 thread3 = new P3(br);
        // thread3.interrupt();
        // System.out.println("p5| p3 canceled\n");
        // P4 thread4 = new P4();
        // thread4.interrupt();
        // System.out.println("p5| p4 canceled\n");
        // P6 thread6 = new P6(br);
        // thread6.interrupt();
        // System.out.println("p5| p6 canceled\n");
    }
}

class P6 extends Thread
{
    private static VectorBuffer buff = new VectorBuffer();
    
    private CyclicBarrier br;
    P6(CyclicBarrier brInit) 
    {
        super("P6");
        br = brInit;
        start();
    }
    public void run() 
    {
        System.out.println("Thread 6 is started\n");
        while(true)
        {
            if (buff.isExit() || VectorBuffer.num_of_operations <= 0) break;
            
            //циклічний бар'єр
            System.out.println("\nThread_2  waits until Thread_1 reaches synchronization point");
            try
            {
              /* Потік 2 чекає, доки потік 1 дійде до точки синхронізації */  
              br.await();
              
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
            VectorBuffer.num_of_operations--;
        }
        
        // P1 thread1 = new P1();
        // thread1.interrupt();
        // System.out.println("p6| p1 canceled\n");
        // P2 thread2 = new P2();
        // thread2.interrupt();
        // System.out.println("p6| p2 canceled\n");
        // P3 thread3 = new P3(br);
        // thread3.interrupt();
        // System.out.println("p6| p3 canceled\n");
        // P4 thread4 = new P4();
        // thread4.interrupt();
        // System.out.println("p6| p4 canceled\n");
        // P5 thread5 = new P5();
        // thread5.interrupt();
        // System.out.println("p6| p5 canceled\n");
    }
}

class Main {
    //private static VectorBuffer buff = new VectorBuffer();
    private static  CyclicBarrier br = new CyclicBarrier(2);

    public static void main(String[] args) {
        int first_length = 4;
        Random rand = new Random();

        for (int i = 0; i < first_length; i++) {
            System.out.println(VectorBuffer.buff.Set(rand.nextInt(1000)));
        }

        System.out.println("Array filled by elements from 0-th to " + (first_length - 1)+ "\n");

        P1 thread1 = new P1();
        P2 thread2 = new P2();
        P3 thread3 = new P3(br);
        P4 thread4 = new P4();
        P5 thread5 = new P5();
        P6 thread6 = new P6(br);

        try
        {
            thread1.join();
            thread2.join();
            thread3.join();
            thread4.join();
            thread5.join();
            thread6.join();
        }
        catch(InterruptedException e)
        {
            System.out.println("Main thred intrerapted");
        }




    }
}
