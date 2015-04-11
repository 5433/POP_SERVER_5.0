package pop.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ResultCommunicator extends Thread {

    private ArrayList messagesToSend = new ArrayList();
    private ResultLogger resultLogger;
    private PrintWriter printer;
    private Socket socket;
    private BufferedReader br;
    String receivedM = "";

    public ResultCommunicator(Socket s, ResultLogger ml) throws IOException {
        resultLogger = ml;
        socket = s;
        printer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public String getMessage() {
        return receivedM;
    }

    //adds String message to arraylist messagesToSend.
    public synchronized void addToMessages(String text) {
        messagesToSend.add(text);
        notify();
    }

    //removes message from arraylist, and returns String message.
    private synchronized String nextElementToSend() throws InterruptedException {
        while (messagesToSend.size() == 0) {
            wait();
        }

        String message = (String) messagesToSend.get(0);
        messagesToSend.remove(0);
        return message;
    }

    //send String message to Device using PrintWriter.
    private void sendMessageToDevice(String text) {
        printer.println(text);
        printer.flush();
    }

    public void run() {
        try {
            while (!isInterrupted()) {
                resultLogger.postMessage();
                String message = nextElementToSend();                
                sendMessageToDevice(message);
            }
        } catch (Exception e) {
            //
        }

        resultLogger.getResultCommunicator(socket).interrupt();
        resultLogger.removeResultCommunicator(socket);
    }

}
