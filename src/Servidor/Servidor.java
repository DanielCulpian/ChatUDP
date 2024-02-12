package Servidor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

public class Servidor {
    private static List<InetSocketAddress> clientes = new ArrayList<>();
    private static InetAddress grupo;
    private static MulticastSocket serverSocket;

    public static void main(String[] args) throws IOException {
        grupo = InetAddress.getByName("230.0.0.0");
        serverSocket = new MulticastSocket(9876);
        serverSocket.joinGroup(grupo);

        byte[] data = new byte[1024];
        String mensaje;

        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(data, data.length);
            serverSocket.receive(receivePacket);

            int length = receivePacket.getLength();
            mensaje = new String(receivePacket.getData(), 0, length);

            System.out.println("Mensaje recibido: " + mensaje);

            InetSocketAddress clientAddress = new InetSocketAddress(receivePacket.getAddress(), receivePacket.getPort());

            if (!clientes.contains(clientAddress)) {
                clientes.add(clientAddress);
            }

            difundirMensaje(mensaje);

            try {
                Thread.sleep(100); // Milisegundos
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void difundirMensaje(String mensaje) throws IOException {
        mensaje += "\n";

        byte[] data = mensaje.getBytes();
        for (InetSocketAddress c : clientes) {
            DatagramPacket paquete = new DatagramPacket(data, data.length, c.getAddress(), c.getPort());
            serverSocket.send(paquete);
        }
    }


}
