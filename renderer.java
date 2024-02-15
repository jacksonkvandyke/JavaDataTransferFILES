//Import GUI elements
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter; 
import java.awt.event.WindowEvent;
import java.net.ConnectException;
import java.net.InetSocketAddress;

import javax.swing.JSeparator;

public class renderer {

    hostConnection hostConnection = null;
    connectoHostConnection toConnection = null;

    public renderer(){
        //Create homePage object
        HomePage homePage = new HomePage(this);

    }

    void HostPage(){
        HostPage hostPage = new HostPage(this);

    }

    void ConnectPage(){
        ConnectPage connectPage = new ConnectPage(this);

    }

    public static void main(String[] args){
        //Create renderer
        renderer mainRenderer = new renderer();

    }
    
}

class HomePage {

    Frame frame = null;
    renderer renderer = null;

    public HomePage(renderer renderer){
        this.frame = new Frame("Simple File Transfer");

        //Set homepage frame size
        this.frame.setSize(500, 300);

        //Create grid for elements
        GridLayout grid = new GridLayout(4, 1, 15, 15);
        this.frame.setLayout(grid);

        //Create elements for window
        Label topLabel = new Label("Simple File Transfer");
        Font toplabelFont = new Font("Arial", Font.PLAIN, 46);
        topLabel.setAlignment(1);
        topLabel.setFont(toplabelFont);

        Button hostButton = new Button("Host Connection");
        hostButton.addActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent e) 
            { 
                renderer.hostConnection = new hostConnection();
                frame.setVisible(false);
                renderer.HostPage();
            } 
        }); 
        
        Button connectButton = new Button("Connect to Host");
        connectButton.addActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent e) 
            { 
                frame.setVisible(false);
                renderer.ConnectPage();
            } 
        }); 

        //Add elements to window
        this.frame.add(topLabel);
        this.frame.add(new JSeparator());
        this.frame.add(hostButton);
        this.frame.add(connectButton);

        //Set homepage window visible
        this.frame.setVisible(true);
        
        //Add exception for when window closes
        this.frame.addWindowListener(new WindowAdapter() { 
            @Override
            public void windowClosing(WindowEvent e) 
            { 
                System.exit(0); 
            } 
        }); 

    }

}

class HostPage {

    Frame frame = null;
    renderer renderer = null;

    public HostPage(renderer renderer){
        this.frame = new Frame("Simple File Transfer");
        this.renderer = renderer;

        //Set homepage frame size
        this.frame.setSize(500, 300);

        //Create grid for elements
        GridLayout grid = new GridLayout(6, 1, 15, 5);
        this.frame.setLayout(grid);

        //Create elements for window
        Label topLabel = new Label("Simple File Transfer");
        Font toplabelFont = new Font("Arial", Font.PLAIN, 46);
        topLabel.setAlignment(1);
        topLabel.setFont(toplabelFont);

        Label connectioninfoLabel = new Label("Connection Info:");
        Font infoFont = new Font("Arial", Font.PLAIN, 32);
        connectioninfoLabel.setAlignment(1);
        connectioninfoLabel.setFont(infoFont);

        //Get socket address for address label and port label
        InetSocketAddress address = (InetSocketAddress)renderer.hostConnection.socket.getLocalSocketAddress();
        Label addressLabel = new Label(String.format("Address: %s", address.getAddress().toString().replace("/", "")));
        Font addressFont = new Font("Arial", Font.PLAIN, 24);
        addressLabel.setAlignment(1);
        addressLabel.setFont(addressFont);

        Label portLabel = new Label(String.format("Port: %d", address.getPort()));
        portLabel.setAlignment(1);
        portLabel.setFont(addressFont);

        Label awaitLabel = new Label("Awaiting completion of the connection.");
        awaitLabel.setAlignment(1);

        //Add elements to window
        this.frame.add(topLabel);
        this.frame.add(new JSeparator());
        this.frame.add(connectioninfoLabel);
        this.frame.add(addressLabel);
        this.frame.add(portLabel);
        this.frame.add(awaitLabel);

        //Set homepage window visible
        this.frame.setVisible(true);
        
        //Add exception for when window closes
        this.frame.addWindowListener(new WindowAdapter() { 
            @Override
            public void windowClosing(WindowEvent e) 
            { 
                System.exit(0); 
            } 
        }); 

    }

}

class ConnectPage {

    Frame frame = null;
    renderer renderer = null;

    public ConnectPage(renderer renderer){
        this.frame = new Frame("Simple File Transfer");

        //Set homepage frame size
        this.frame.setSize(500, 400);

        //Create grid for elements
        GridLayout grid = new GridLayout(6, 1, 0, 15);
        this.frame.setLayout(grid);

        //Create elements for window
        Label topLabel = new Label("Simple File Transfer");
        Font toplabelFont = new Font("Arial", Font.PLAIN, 46);
        topLabel.setAlignment(1);
        topLabel.setFont(toplabelFont);

        Label connectioninfoLabel = new Label("Connect to host:");
        Font infoFont = new Font("Arial", Font.PLAIN, 32);
        connectioninfoLabel.setAlignment(1);
        connectioninfoLabel.setFont(infoFont);

        TextField addressEntry = new TextField();
        Font addressFont = new Font("Arial", Font.PLAIN, 32);
        addressEntry.setFont(addressFont);

        TextField portEntry = new TextField();
        Font portFont = new Font("Arial", Font.PLAIN, 32);
        portEntry.setFont(portFont);

        Button connectButton = new Button("Connect");
        Font connectFont = new Font("Arial", Font.PLAIN, 32);
        connectButton.setFont(connectFont);
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                try{
                    renderer.toConnection = new connectoHostConnection(addressEntry.getText(), Integer.valueOf(portEntry.getText()));
                    System.out.println(renderer.toConnection);
                }finally{
                    if (renderer.toConnection == null){
                        return;

                    }

                    //Start next window if successful connection
                    renderer.HostPage();
                    frame.setVisible(false);
                }
            }

        });

        //Add elements to window
        this.frame.add(topLabel);
        this.frame.add(new JSeparator());
        this.frame.add(connectioninfoLabel);
        this.frame.add(addressEntry);
        this.frame.add(portEntry);
        this.frame.add(connectButton);

        //Set homepage window visible
        this.frame.setVisible(true);
        
        //Add exception for when window closes
        this.frame.addWindowListener(new WindowAdapter() { 
            @Override
            public void windowClosing(WindowEvent e) 
            { 
                System.exit(0); 
            } 
        }); 

    }

}
