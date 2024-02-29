//Import GUI elements
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter; 
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.InetSocketAddress;
import javax.swing.JFileChooser;

import javax.swing.JSeparator;

public class renderer {

    hostConnection hostConnection = null;
    connectoHostConnection toConnection = null;

    HomePage homePage = null;
    HostPage hostPage = null;
    ConnectPage connectPage = null;
    ConnectedSessionPage connectedsessionPage = null;

    public renderer(){
        //Create homePage object
        this.homePage = new HomePage(this);

    }

    void HostPage(){
        this.hostPage = new HostPage(this);

    }

    void ConnectPage(){
        this.connectPage = new ConnectPage(this);

    }

    void ConnectedSessionPage(){
        this.connectedsessionPage = new ConnectedSessionPage(this);

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
                renderer.hostConnection = new hostConnection(renderer);
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
                }finally{
                    if (renderer.toConnection == null){
                        return;

                    }

                    //Start next window if successful connection
                    renderer.ConnectedSessionPage();
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

class ConnectedSessionPage {

    ConnectedSessionPage self = this;
    Frame frame = null;
    renderer renderer = null;
    GatherAllFiles filestoSend = null;
    Label statusLabels[] = new Label[2];

    public ConnectedSessionPage(renderer renderer){
        this.frame = new Frame("Simple File Transfer");

        //Set homepage frame size
        this.frame.setSize(500, 400);

        //Create grid for elements
        GridLayout grid = new GridLayout(6, 1, 0, 15);
        this.frame.setLayout(grid);

        //Create elements for initial window
        Label topLabel = new Label("Simple File Transfer");
        Font toplabelFont = new Font("Arial", Font.PLAIN, 46);
        topLabel.setAlignment(1);
        topLabel.setFont(toplabelFont);

        Button selectFileButton = new Button("Select Files");
        Font connectFont = new Font("Arial", Font.PLAIN, 32);
        selectFileButton.setFont(connectFont);
        selectFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                //Prompt user to select a directory of files or file
                new SelectFile(self);
            }

        });

        //Create UI elements for file await, but set them invisible unit needed
        Font labelFont = new Font("Arial", Font.PLAIN, 32);
        statusLabels[0] = new Label();
        statusLabels[0].setText("Processing files for transfer.");
        statusLabels[0].setFont(labelFont);
        statusLabels[0].setVisible(false);

        statusLabels[1] = new Label();
        statusLabels[1].setText("0");
        statusLabels[1].setFont(labelFont);

        //Add elements to window
        this.frame.add(topLabel);
        this.frame.add(new JSeparator());
        this.frame.add(selectFileButton);
        this.frame.add(statusLabels[0]);
        this.frame.add(statusLabels[1]);
        statusLabels[1].setVisible(false);

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

class SelectFile {
    //This class allows the user to select a file

    SelectFile(ConnectedSessionPage parent){
        //Prompt file select
        File userPrompt = new File("C://Program Files//");

        //Try to open and get file directory or file
        if (Desktop.isDesktopSupported()){
            try{
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(userPrompt);
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                chooser.showOpenDialog(chooser);

                userPrompt = chooser.getSelectedFile();
            }catch(SecurityException e){
                System.out.println(e);
            }
        }

        //Create thread that waits for file preperation
        WaitForFiles threadObject = new WaitForFiles(userPrompt, parent);
        Thread thread = new Thread(threadObject);
        thread.start();

    }
}

class WaitForFiles extends Thread{
    //This file waits for the users files to be prepared

    File userPrompt = null;
    ConnectedSessionPage parent = null;
    Label labels[] = null;
 
    WaitForFiles(File userPrompt, ConnectedSessionPage parent){
        this.userPrompt = userPrompt;
        this.parent = parent;
        this.labels = parent.statusLabels;

    }

    public void run() {
        //Continues to loop until all files have been gathered
        GatherAllFiles files = new GatherAllFiles(this.userPrompt);
        this.labels[0].setVisible(true);
        this.labels[1].setVisible(true);

        while (true){
            //Update UI on loop about progress
            this.labels[1].setText(String.valueOf(files.currentFiles / files.requiredFiles * 100));
            this.parent.frame.validate();

            if (files.progress == 100){
                this.labels[0].setText("Files ready for transfer.");
                parent.filestoSend = files;
                this.parent.frame.validate();
                return;
            }

            try{
                Thread.sleep(5);
            }catch (InterruptedException e){
                System.out.print(e);
            }
        }

    }

}
