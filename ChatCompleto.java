
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
