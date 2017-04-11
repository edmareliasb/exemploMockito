package br.com.estudo.mock.leilao.servico;

import java.util.Calendar;

public class RelogioDoSistema implements Relogio {

    public Calendar hoje() {
        return Calendar.getInstance();
    }

}
