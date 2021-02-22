import java.io.IOException;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.util.Scanner;

// Some info and context about DadBot!!! (programmed and designed by Josh Taylor)
// Dadbot exists to periodically remind the server channel that he loves his children.
// He can give you information about the server and who is in the current channel.
// He can also tell you what time it is, and leave the server respectfully, to which he says "Bye Kids", but his quit message says "i miss my kids already..."
// Additionally dad bot will look for any 'im' in the chat and reply with, "hi ______, I'm dad hahaha"
// So if you were to say, "I'm hungry", dadbot would reply, "hi hungry, I'm dad. Hahaha!"

// Compile with: javac *.java
// or: javac IRC_DadBot.java
// Run with: java IRC_DadBot.java


public class IRC_DadBot{

  // Declare private IO variables
  private static PrintWriter out;
  private static Scanner in;

  // Writes message to server and to command line
  private static void write(String command, String message){
    String fullMessage = command + " " + message;
    System.out.println(">>> " + fullMessage);
    out.print(fullMessage + "\r\n");
    out.flush();
  }

  public static void main(String[] args) throws IOException{

    Scanner console = new Scanner(System.in);
    String defaultHostname = "selsey.nsqdc.city.ac.uk";
    int defaultPort = 6667;
    String defaultNickname = "dadbot";

    // Gets initial input from user
    System.out.println("Enter the hostname/IP address you'd like to join.");
    System.out.println("Leave empty for default. Default is: " + defaultHostname);
    String hostname = console.nextLine();
    if (hostname == ""){
      hostname = defaultHostname;
    }

    int portNum;
    System.out.println("Enter the port number you'd like to use.");
    System.out.println("Leave empty for default. Default is: " + defaultPort);
    String port = console.nextLine();
    if (port.isEmpty()){
       portNum = defaultPort;
    }
    else{
      portNum = Integer.parseInt(port);
    }

    System.out.println("Enter your nickname: ");
    System.out.println("Leave empty for default. Default is: " + defaultNickname);
    String nickName = console.nextLine();
    if (nickName.isEmpty()){
      nickName = defaultNickname;
    }

    System.out.println("Enter your username (can be anything): ");
    String userName = console.nextLine();

    System.out.println("Enter your real name (can be anything): ");
    String realName = console.nextLine();

    System.out.println("Enter the channel name you'd like to join (include #): ");
    String channelName = console.nextLine();

    // ------------------------------------------------------------------------
    // Connects to server
    System.out.println("Connecting to " + hostname + "...");
    Socket sock = new Socket(hostname, portNum);

    // Creates input and output streams
    out = new PrintWriter(sock.getOutputStream(), true);
    in = new Scanner(sock.getInputStream());

    // writes commands to server once connected
    write("NICK", nickName); // COMMAND #1
    write("USER", (userName + " 0 * :" + realName)); // COMMAND #2
    write("JOIN", channelName); //COMMAND #3

    String initialMessage = "HELLO! I am your dad Bot! Use \'!\' to get my attention. " +
    "Just type \'!help\' if you have any questions, kiddo!";
    write("PRIVMSG ", channelName + " :" + initialMessage); // COMMAND #4

    boolean switchPongMessages = true; // Declare var

    // ----------------------------- Loop for bot interaction -----------------------------
    while (in.hasNext()){

      // Gathers input and prints
      String serverMessage = in.nextLine();
      System.out.println("<<< " + serverMessage);

      // Bot responds to PING with PONG (every 30 seconds or so of idle time)
      if (serverMessage.startsWith("PING")){
        String pingContents = serverMessage.split(" ", 2)[1];
        write("PONG", pingContents); // COMMAND #5

        // Switches between 2 pong messages
        if (switchPongMessages == true){
          // Remind your children that you love them
          write("PRIVMSG", channelName + " :" + "Just wanted to remind you that I\'m so blessed to be the father of a such a great group of kids. I love you guys!");
          switchPongMessages = false;
        }
        else{
          // Give hint to use dad joke
          write("PRIVMSG", channelName + " :" + "Psst. Use \"I\'m\" in a chat message for a hidden dadbot feature.");
          switchPongMessages = true;
        }

      }

      // If a user on the channel sends a message
      if (serverMessage.contains("PRIVMSG")){
        String userMessage = serverMessage.split(":", 3)[2].toLowerCase();
        System.out.println("User said: " + userMessage);

        // If a user on the channel is wanting to communicate with us!
        if (userMessage.substring(0, 1).equals("!")){

          String userCommand = userMessage.substring(1);
          System.out.println("User command: " + userCommand);

          // Handles each command case
          switch (userCommand){

            // ----- Prints out each available command to the user -----
            case "help":
              write("PRIVMSG ", channelName + " :" + "List of commands:");
              write("PRIVMSG ", channelName + " :" + "!help - Prints all commands");
              write("PRIVMSG ", channelName + " :" + "!info - Prints information about the server");
              write("PRIVMSG ", channelName + " :" + "!leave - Kicks me from the server. Unfortunate, but I understand if you need some space kiddo...");
              write("PRIVMSG ", channelName + " :" + "!names - Prints the nicknames of every user in the current channel");
              write("PRIVMSG ", channelName + " :" + "!time - Prints the current date and time at the server's location");
              break;

            // ----- Prints out the names of each user in the current channel -----
            case "names":
              write("NAMES", channelName); // COMMAND #6
              // Grabs correct info from server response
              while (in.hasNext()){
                String nameContents = in.nextLine();
                nameContents = nameContents.split("=", 2)[1];
                // Print to channel
                write("PRIVMSG", channelName + " :" + "All names in" + nameContents);
                break;
              }
              break;

            // ----- Prints out date and time in server location -----
            case "time":
              write("TIME", hostname); // COMMAND #7
              // Grabs proper info from server response
              while (in.hasNext()){
                String serverResponse = in.nextLine();
                String timeContents = serverResponse.split(":", 3)[2];
                String serverName = serverResponse.split(nickName, 2)[1];
                serverName = serverName.split(":", 2)[0];
                // Prints to channel
                write("PRIVMSG", channelName + " :" + "Current datetime at" + serverName + ": " + timeContents);
                break;
              }
              break;

            // ----- Prints out server information to the channel -----
            case "info":
              write("INFO", hostname); // COMMAND #8
              write("PRIVMSG", channelName + " :" + "Here is some information about this IRC server:");
              // Grabs correct info from server response
              while (in.hasNext()){
                String serverInfo = in.nextLine();

                // Breaks from loop once last line is found
                if (serverInfo.contains("End of INFO")){
                  break;
                }

                // Narrows down line to correct info and then prints
                serverInfo = serverInfo.split(":", 3)[2];
                write("PRIVMSG ", channelName + " :" + serverInfo);
              }
              break;

            // ----- Exits the server gracefully -----
            case "leave":
              write("PRIVMSG ", channelName + " :" + "Bye kids!");
              write("QUIT", " :" + "I miss my kids already..."); // COMMAND #9
              break;

            // If command is not recognized
            default:
              write("PRIVMSG", channelName + " :" + "Didn't understand you there son. Type \'!help\' for help.");
          }

        }

        // If the message is not a command i.e. is a regular message --> check for word 'im' --> reply with witty dad response
        else{
          // check for 'im'
          if (serverMessage.contains("im") || serverMessage.contains("i\'m")){
            // Removes apostrophe for simplicity
            String tempName = serverMessage.replace("i\'m", "im");
            // Grabs the word(s) after 'im'
            tempName = tempName.split("im", 2)[1];
            tempName = tempName.split(" ", 2)[1]; // This removes the space after im --> meaning it includes ~all~ words after 'im' in message
            // it's not what i originally intended, but it's much funnier this way!
            // for example, in response to "im so bored", the bot says, "hi so bored, im dad!" instead of "hi so, im dad!"

            // Replies with dad message
            write("PRIVMSG", channelName + " :" + "Hi " + tempName + ", I\'m dad! Hahaha!");
          }

        }
      }

    }

    // Cleaning up
    in.close();
    out.close();
    sock.close();

    System.out.println("Goodbye!");

  }
}
