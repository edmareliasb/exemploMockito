package br.com.estudo.mock.leilao.servico;

import br.com.estudo.mock.leilao.dominio.Leilao;

public interface EnviadorDeEmail {
	 void envia(Leilao leilao);
}
