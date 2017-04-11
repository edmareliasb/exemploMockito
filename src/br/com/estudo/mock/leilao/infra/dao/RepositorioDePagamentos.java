package br.com.estudo.mock.leilao.infra.dao;

import br.com.estudo.mock.leilao.dominio.Pagamento;

public interface RepositorioDePagamentos {

    void salva(Pagamento pagamento);
}
