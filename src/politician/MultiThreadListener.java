package politician;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiThreadListener {

	final int port;
	ServerSocket socket;
	Class<? extends ScrabblosPolitician> request_executor;

	
	public MultiThreadListener(int port, Class<? extends ScrabblosPolitician> re) {
		super();
		this.port = port;
		this.request_executor = re;
	}
	
	public Class<? extends ScrabblosPolitician> getRequestExecutor() {
		return request_executor;
	}

	public void setRequestExecutor(Class<? extends ScrabblosPolitician> re) {
		this.request_executor = re;
	}

	public int getPort() {
		return port;
	}

	public void startListening() {
		try {
			socket = new ServerSocket(port);
			while (true) {
				Socket socket_connexion = socket.accept();
				ScrabblosPolitician res;
				res = request_executor.getConstructor(Socket.class).newInstance(socket_connexion);
				new Thread(res).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
