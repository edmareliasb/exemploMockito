package br.com.estudo.mock.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Matchers.any;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;
import org.mockito.InOrder;

import br.com.estudo.mock.leilao.builder.CriadorDeLeilao;
import br.com.estudo.mock.leilao.dominio.Leilao;
import br.com.estudo.mock.leilao.infra.dao.RepositorioDeLeiloes;

public class EncerradorDeLeilaoTest {

	@Test
	public void deveEncerrarLeiloesQueComecaramUmaSemanaAtras() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(antiga).constroi();

		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);

		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		// ensinando o mock a reagir da maneira que esperamos!
		when(daoFalso.correntes()).thenReturn(leiloesAntigos);

		EnviadorDeEmail carteiroFalse = mock(EnviadorDeEmail.class);

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalse);
		encerrador.encerra();

		assertEquals(2, encerrador.getTotalEncerrados());
		assertTrue(leilao1.isEncerrado());
		assertTrue(leilao2.isEncerrado());
	}

	@Test
	public void naoDeveEncerrarLeiloesQueComecaramOntem() {
		Calendar ontem = Calendar.getInstance();
		ontem.add(Calendar.DAY_OF_MONTH, -1);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(ontem).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(ontem).constroi();

		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);

		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		// ensinando o mock a reagir da maneira que esperamos!
		when(daoFalso.correntes()).thenReturn(leiloesAntigos);

		EnviadorDeEmail carteiroFalse = mock(EnviadorDeEmail.class);

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalse);
		encerrador.encerra();

		assertEquals(0, encerrador.getTotalEncerrados());
		assertFalse(leilao1.isEncerrado());
		assertFalse(leilao2.isEncerrado());

		verify(daoFalso, never()).atualiza(leilao1);
		verify(daoFalso, never()).atualiza(leilao2);

	}

	@Test
	public void naoDeveEncerrarLeiloesCasoNaoHajaNenhum() {
		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);

		when(daoFalso.correntes()).thenReturn(new ArrayList<Leilao>());

		EnviadorDeEmail carteiroFalse = mock(EnviadorDeEmail.class);

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalse);
		encerrador.encerra();

		assertEquals(0, encerrador.getTotalEncerrados());
	}

	@Test
	public void deveAtualizarLeiloesEncerrados() {

		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();

		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));

		EnviadorDeEmail carteiroFalse = mock(EnviadorDeEmail.class);

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalse);
		encerrador.encerra();

		// verificando que o metodo atualiza foi realmente invocado!
		verify(daoFalso, times(1)).atualiza(leilao1);
	}

	@Test
	public void deveEnviarEmailAposPersistirLeilaoEncerrado() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();

		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));

		EnviadorDeEmail carteiroFalso = mock(EnviadorDeEmail.class);

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();

		/*
		 * garanta que os métodos foram executados nessa ordem específica:
		 * primeiro o DAO, depois o envio do e-mail
		 */
		// passamos os mocks que serao verificados
		InOrder inOrder = inOrder(daoFalso, carteiroFalso);
		// a primeira invocação
		inOrder.verify(daoFalso, times(1)).atualiza(leilao1);
		// a segunda invocação
		inOrder.verify(carteiroFalso, times(1)).envia(leilao1);
	}

	@Test
	public void deveContinuarAExecucaoMesmoQuandoDaoFalha() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(antiga).constroi();

		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

		doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao1);

		EnviadorDeEmail carteiroFalso = mock(EnviadorDeEmail.class);

		EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerradorDeLeilao.encerra();

		verify(carteiroFalso, never()).envia(leilao1);
		verify(daoFalso).atualiza(leilao2);
		verify(carteiroFalso).envia(leilao2);
	}

	@Test
	public void deveContinuarAExecucaoMesmoQuandoEnviadorDeEmaillFalha() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(antiga).constroi();

		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

		EnviadorDeEmail carteiroFalso = mock(EnviadorDeEmail.class);
		doThrow(new RuntimeException()).when(carteiroFalso).envia(leilao1);

		/**
		 * O teste falha, mas porque o Mockito não consegue fazer com que o
		 * método lance Exception. Exception é uma exceção checada no Java, e
		 * seu lançamento precisa ser explicitamente declarado, o que não
		 * acontece no método envia() do EnviadorDeEmail. 
		 * 
		 * doThrow(new Exception()).when(carteiroFalso).envia(leilao1);
		 */

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();

		verify(daoFalso).atualiza(leilao2);
		verify(carteiroFalso).envia(leilao2);
	}
	
	@Test
    public void deveDesistirSeDaoFalhaPraSempre() {
        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();
        Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(antiga).constroi();

        RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

        //doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao1);
       // doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao2);

        doThrow(new RuntimeException()).when(daoFalso).atualiza(any(Leilao.class));

        EnviadorDeEmail carteiroFalso = mock(EnviadorDeEmail.class);

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
        encerrador.encerra();

      //  verify(carteiroFalso, never()).envia(leilao1);
      //  verify(carteiroFalso, never()).envia(leilao2);
       verify(carteiroFalso, never()).envia(any(Leilao.class));
    }

}
