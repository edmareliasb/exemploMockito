package br.com.estudo.mock.leilao.infra.dao;

import java.util.List;

import br.com.estudo.mock.leilao.dominio.Leilao;

public interface RepositorioDeLeiloes {

	void salva(Leilao leilao);

	List<Leilao> encerrados();

	List<Leilao> correntes();

	void atualiza(Leilao leilao);
}
