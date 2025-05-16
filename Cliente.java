
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Cliente {
    private JFrame frame = new JFrame("Chat");
    private JTextArea area = new JTextArea(20, 50);
    private JTextField campo = new JTextField(40);
    private JButton botao = new JButton("Enviar");
    private PrintWriter out;

    public Cliente() {
        area.setEditable(false);
        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel painel = new JPanel(new BorderLayout());
        painel.add(campo, BorderLayout.CENTER);
        painel.add(botao, BorderLayout.EAST);
        frame.add(painel, BorderLayout.SOUTH);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        botao.addActionListener(e -> enviar());
        campo.addActionListener(e -> enviar());

        iniciarConexao();
    }

    private void enviar() {
        String msg = campo.getText();
        if (!msg.trim().isEmpty()) {
            out.println(msg);
            campo.setText("");
        }
    }

    private void iniciarConexao() {
        try {
            Socket socket = new Socket("localhost", 12345);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String nome = JOptionPane.showInputDialog(frame, "Digite seu nome:");
            while (nome == null || nome.trim().isEmpty()) {
                nome = JOptionPane.showInputDialog(frame, "Nome inválido. Digite novamente:");
            }
            out.println(nome);

            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        area.append(msg + "\n");
                    }
                } catch (IOException e) {
                    area.append("Conexão encerrada.\n");
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Erro ao conectar ao servidor: " + e.getMessage());
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Cliente());
    }
}
