package ClientServer;

import java.io.*;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


public class Client implements Runnable {

	private ClientThread client = null;
	private BufferedReader in = null;
	private BufferedReader r = null;
	private SSLSocket directorSocket = null;
	private SSLSocket bankSocket = null;
	private Thread thread = null;
	private PrintWriter directorOut = null;
	private PrintWriter bankOut = null;
	
	
	public static void main(String args[])
	{
		String str = "";
		BufferedReader sysIn = new BufferedReader(new InputStreamReader(System.in));
		while(true)
		{
			System.out.println();
			System.out.println("Please enter server addresses and ports in the form,");
			System.out.println("'DIRECTOR_ADDRESS PORT BANK_ADDRESS PORT'");
			System.out.print("(e.g. '127.0.0.1 1234 192.168.0.1 4321'): ");
			try 
			{
				str = sysIn.readLine();
				if(str.equals(""))
				{
					str = "127.0.0.1 1111 127.0.0.1 2222";
				}
				if(str.equals("."))
				{
					break;
				}
				String strs[] = str.split(" ");
				Client client = new Client(strs[0], Integer.parseInt(strs[1]), strs[2], Integer.parseInt(strs[3]));
				Thread thread = client.getThread();
				while(thread.isAlive() && thread != null)
				{
					Thread.sleep(1);
				}
			} 
			catch (IOException ioe) 
			{
				System.out.println("Error reading from console: " + ioe.getMessage());
			}
			catch (Exception e)
			{
				System.out.println("One or more addresses or ports are incorrect!");
			}
		}
		System.out.println("Goodbye.");
	}
	
	//Print out for socket
	private static void printSocketInfo(SSLSocket s) {
		System.out.println("Socket class: "+s.getClass());
		System.out.println("   Remote address = "
				+s.getInetAddress().toString());
		System.out.println("   Remote port = "+s.getPort());
		System.out.println("   Local socket address = "
				+s.getLocalSocketAddress().toString());
		System.out.println("   Local address = "
				+s.getLocalAddress().toString());
		System.out.println("   Local port = "+s.getLocalPort());
		System.out.println("   Need client authentication = "
				+s.getNeedClientAuth());
		SSLSession ss = s.getSession();
		System.out.println("   Cipher suite = "+ss.getCipherSuite());
		System.out.println("   Protocol = "+ss.getProtocol());
	}
	
	public Client(String directorName, int directorPort, String bankName, int bankPort)
	{
		SSLSocketFactory f = 
				(SSLSocketFactory) SSLSocketFactory.getDefault();
		try {
			directorSocket = (SSLSocket) f.createSocket(directorName, directorPort);
			printSocketInfo(directorSocket);
			directorSocket.startHandshake();
			bankSocket = (SSLSocket) f.createSocket(bankName, bankPort);
			printSocketInfo(bankSocket);
			bankSocket.startHandshake();	
			start();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			stop();
		}
	     
	}

	protected void finalize( ) throws Throwable
	{
		directorOut.println(".");
		bankOut.println(".");
		super.finalize();
	}
	
	public void handle(String msg)
	{
		if(msg.equals("."))
		{
			stop();
		}
		else System.out.println(msg);
	}

	@Override
	public void run()
	{
		System.out.println("Welcome!");
		while (thread != null)
		{
			try
			{
				String str = in.readLine();
				if(str.equals("."))
				{
					directorOut.println(str);
					directorOut.flush();
					bankOut.println(str);
					bankOut.flush();
				}
				if(str.startsWith(".director "))
				{
					directorOut.println(str);
					directorOut.flush();
				}
				if(str.startsWith(".bank "))
				{
					bankOut.println(str);
					bankOut.flush();
				}
				Thread.sleep(10);
			}
			catch (IOException ioe)
			{
				System.out.println("Console Error: " + ioe.getMessage());
				stop();
			} 
			catch (InterruptedException e) { }
		}
	}

	public void start() throws IOException
	{
		in = new BufferedReader(
				new InputStreamReader(System.in));
		directorOut = new PrintWriter(directorSocket.getOutputStream());
		bankOut = new PrintWriter(bankSocket.getOutputStream());
		if(thread == null)
		{
			client = new ClientThread(this,directorSocket);
			client = new ClientThread(this,bankSocket);
			thread = new Thread(this);
			thread.start();
		}
	}

	public void stop()
	{
		System.out.println("Disconnecting from server ...");
		if(thread != null)
		{
			thread.interrupt();
			thread = null;
		}
		try 
		{
			if(directorOut != null) directorOut.close();
			if(directorSocket != null) directorSocket.close();
			if(bankOut != null) bankOut.close();
			if(bankSocket != null) bankSocket.close();
		}
		catch (IOException ioe)
		{
			System.out.println("Error closing ...");
		}

		client.close();
		client.interrupt();
	}
	
	public Thread getThread()
	{
		return thread;
	}

}
