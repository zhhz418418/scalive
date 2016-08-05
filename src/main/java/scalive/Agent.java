package scalive;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.ServerSocket;
import java.net.Socket;

public class Agent {
    /**
     * @param agentArgs <jarSearchDir1>[<File.pathSeparator><jarSearchDir2>...] <server port>
     *
     * <pre>{@code
     * jarSearchDir/
     *   scalive.jar
     *
     *   scala-library-2.11.0.jar
     *   scala-compiler-2.11.0.jar
     *   scala-reflect-2.11.0.jar
     *
     *   [Other Scala versions]
     * }</pre>
     */
    public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
        final String[] args          = agentArgs.split(" ");
        final String[] jarSearchDirs = args[0].split(File.pathSeparator);
        final int      port          = Integer.parseInt(args[1]);

        System.out.println("[Scalive] REPL server starts at port " + port);
        final ServerSocket server = new ServerSocket(port);

        // Need to start a new thread because:
        // - ServerSocket#accept blocks until a connection comes in
        // - VirtualMachine#loadAgent at the client does not return until this
        //   agentmain method returns
        // - The client only connects to the server after VirtualMachine#loadAgent
        //   returns
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket client = server.accept();  // Block until a connection comes in
                    server.close();                   // Accept no other clients
                    Server.serve(client, jarSearchDirs);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
