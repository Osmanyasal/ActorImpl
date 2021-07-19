package philosophers.arge.actor;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		System.out.println("START THREAD");
		Thread thread = new Thread(new ThreadRunner());
		thread.setDaemon(true);
		thread.start();
		System.out.println("end of main thread");

	}
}

class ThreadRunner implements Runnable {

	@Override
	public void run() {
		while (true) {
			System.out.println("EXECUTE THREAD");
		}
	}
}