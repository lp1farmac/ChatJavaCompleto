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

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Servidor {
    private static final int PORTA = 12345;
    private static final Set<ClienteHandler> clientes = ConcurrentHashMap.newKeySet();
    private static final Map<ClienteHandler, String> nomes = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            System.out.println("Servidor iniciado na porta " + PORTA);

            while (true) {
                Socket socket = serverSocket.accept();
                ClienteHandler cliente = new ClienteHandler(socket);
                clientes.add(cliente);
                new Thread(cliente).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClienteHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String nome;

        public ClienteHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Digite seu nome:");
                nome = in.readLine();
                while (nome == null || nome.trim().isEmpty() || nomes.containsValue(nome)) {
                    out.println("Nome inválido ou já em uso. Digite outro nome:");
                    nome = in.readLine();
                }
                nomes.put(this, nome);
                broadcast(nome + " entrou no chat.");

                String msg;
                while ((msg = in.readLine()) != null) {
                    if (msg.startsWith("@")) {
                        String[] partes = msg.split(" ", 2);
                        if (partes.length < 2) {
                            out.println("Formato: @usuario mensagem");
                            continue;
                        }
                        String destino = partes[0].substring(1);
                        String conteudo = partes[1];
                        boolean encontrado = false;
                        for (Map.Entry<ClienteHandler, String> entry : nomes.entrySet()) {
                            if (entry.getValue().equals(destino)) {
                                entry.getKey().out.println("(Privado) " + nome + ": " + conteudo);
                                out.println("(Privado para " + destino + "): " + conteudo);
                                encontrado = true;
                                break;
                            }
                        }
                        if (!encontrado) {
                            out.println("Usuário não encontrado.");
                        }
                    } else {
                        broadcast(nome + ": " + msg);
                    }
                }
            } catch (IOException e) {
                System.out.println("Cliente desconectado: " + nome);
            } finally {
                try {
                    clientes.remove(this);
                    broadcast(nome + " saiu do chat.");
                    nomes.remove(this);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcast(String mensagem) {
            for (ClienteHandler c : clientes) {
                if (c != this) {
                    c.out.println(mensagem);
                }
            }
        }
    }
}

import javax.swing.*;

public class ChatCompleto {
    public static void main(String[] args) {
        String[] opcoes = {"Servidor", "Cliente"};
        int escolha = JOptionPane.showOptionDialog(
            null,
            "Deseja iniciar como Servidor ou Cliente?",
            "Escolha",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            opcoes,
            opcoes[0]
        );
import javax.swing.*;

public class ChatCompleto {
    public static void main(String[] args) {
        String[] opcoes = {"Servidor", "Cliente"};
        int escolha = JOptionPane.showOptionDialog(
            null,
            "Deseja iniciar como Servidor ou Cliente?",
            "Escolha",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            opcoes,
            opcoes[0]
        );

        if (escolha == 0) {
            Servidor.main(args);
        } else {
            Cliente.main(args);
        }
    }
}
        if (escolha == 0) {
            Servidor.main(args);
        } else {
            Cliente.main(args);
        }
    }
}
