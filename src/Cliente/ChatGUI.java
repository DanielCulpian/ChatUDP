package Cliente;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class ChatGUI extends JFrame {
    private JPanel mainPanel;
    private JPanel chatPanel;
    private JPanel userListPanel;
    private JPanel actionsPanel;
    private JLabel label01;
    private JTextField textBox;
    private JButton sendBtn;
    private JTextArea chatArea;
    private JList userList;
    private JScrollPane chatScrollPane;
    private String user;
    private List<String> conectedUsers = new LinkedList<>();
    private Vector<String> ve;
    private MulticastSocket clientSocket;
    private InetAddress grupo;

    public ChatGUI(String user) {

        this.user = user;

        setTitle("Chat: " + user);
        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setVisible(true);
        setSize(650, 750);

        sendBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mandarMensaje(textBox.getText());
            }
        });

        try{
            grupo = InetAddress.getByName("230.0.0.0");
            clientSocket = new MulticastSocket(9876);
            clientSocket.joinGroup(grupo);
        }catch(IOException ioe){
            System.out.println(ioe.getMessage());

        }

        new Thread(() -> {
            try {
                byte[] data = new byte[1024];
                while (true) {
                    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                    clientSocket.receive(receivePacket);

                    String mensajeFormateado = new String(receivePacket.getData(), 0, receivePacket.getLength());

                    if(mensajeFormateado.toLowerCase().contains("user")){
                        String partes[] = mensajeFormateado.split("\\|");
                        String comandoSpliteado[] = partes[0].split(",");
                        if(comandoSpliteado[0].equalsIgnoreCase("cuser")){
                            if(!conectedUsers.contains(comandoSpliteado[1])){
                                conectedUsers.add(comandoSpliteado[1]);
                                actualizarListaUsuarios();
                                actualizarChat(partes[1]);
                            }else{
                                actualizarChat(partes[1]);
                            }
                        } else if (comandoSpliteado[0].equalsIgnoreCase("duser")) {
                            conectedUsers.remove(comandoSpliteado[1]);
                            actualizarListaUsuarios();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void actualizarChat(String nuevoMsg){
        if(chatArea.getText().isEmpty() || chatArea.getText().isBlank()){
            chatArea.setText(nuevoMsg);
        }else{
            chatArea.setText(chatArea.getText() + "\n" + nuevoMsg);
        }
    }

    private void actualizarListaUsuarios(){
        ve  = new Vector<>(conectedUsers);
        userList.setListData(ve);
    }

    private void mandarMensaje(String texto){
        String textoEnvio = "CUser," + this.user + "|" + this.user + ": " + texto;
        byte[] data = textoEnvio.getBytes();

        DatagramPacket paquete = new DatagramPacket(data, data.length, grupo, 9876);
        try{
            clientSocket.send(paquete);
            textBox.setText("");
        }catch (IOException ioe){
            System.out.println(ioe.getMessage());
        }
    }

    @Override
    public void dispose(){
        String textoEnvio = "DUser," + this.user;
        byte[] data = textoEnvio.getBytes();

        DatagramPacket paquete = new DatagramPacket(data, data.length, grupo, 9876);
        try{
            clientSocket.send(paquete);
        }catch (IOException ioe){
            System.out.println(ioe.getMessage());
        }

        super.dispose();
        System.exit(0);
    }
}
